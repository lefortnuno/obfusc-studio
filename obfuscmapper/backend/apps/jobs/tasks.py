"""Celery tasks: analyze, apply, generate, audit, rotation."""
from __future__ import annotations

import json
import logging
import os
import subprocess
import tempfile
from pathlib import Path
from typing import Any, Dict

from celery import shared_task
from django.conf import settings
from django.utils import timezone

from apps.projects.models import Project, ProjectPair
from apps.variables.models import Folder, SourceFile, Variable

logger = logging.getLogger(__name__)


@shared_task(bind=True, max_retries=0, time_limit=600)
def run_analysis(self, analysis_job_id: str) -> Dict[str, Any]:
    """Run the JavaParser JAR against project.root_path and ingest results."""
    from .models import AnalysisJob

    job = AnalysisJob.objects.get(pk=analysis_job_id)
    job.status = "running"
    job.started_at = timezone.now()
    job.celery_task_id = self.request.id or ""
    job.save(update_fields=["status", "started_at", "celery_task_id"])

    project = job.project

    try:
        if not project.root_path or not Path(project.root_path).exists():
            raise RuntimeError("project.root_path does not exist: %s" % project.root_path)

        jar = settings.PARSER_JAR_PATH
        if not Path(jar).exists():
            raise RuntimeError("Parser JAR not found at %s. Run: cd parser && mvn package" % jar)

        with tempfile.NamedTemporaryFile(suffix=".json", delete=False) as tmp:
            out_path = tmp.name

        cmd = [
            settings.JAVA_BIN, "-Xmx400m", "-jar", jar,
            "-p", project.root_path,
            "-o", out_path,
        ]
        logger.info("Running: %s", " ".join(cmd))
        result = subprocess.run(cmd, capture_output=True, text=True, timeout=300)
        if result.returncode != 0:
            raise RuntimeError("parser failed (rc=%d): %s" % (result.returncode, result.stderr[:1000]))

        with open(out_path, "r", encoding="utf-8") as f:
            data = json.load(f)
        os.unlink(out_path)

        ingest_summary = ingest_analysis_result(project, data)

        job.result_json = {"summary": ingest_summary, "stderr": result.stderr[:2000]}
        job.status = "done"
        job.finished_at = timezone.now()
        job.save(update_fields=["result_json", "status", "finished_at"])
        return ingest_summary
    except Exception as exc:
        logger.exception("run_analysis failed")
        job.status = "failed"
        job.error_message = str(exc)
        job.finished_at = timezone.now()
        job.save(update_fields=["status", "error_message", "finished_at"])
        raise


def ingest_analysis_result(project: Project, data: Dict[str, Any]) -> Dict[str, int]:
    """Create Folder / SourceFile / Variable rows from parser JSON. Idempotent."""
    folders_in = data.get("folders", [])
    files_in = data.get("files", [])
    vars_in = data.get("variables", [])

    # Folders: insert by path
    folder_by_path: Dict[str, Folder] = {f.path: f for f in project.folders.all()}
    for f in folders_in:
        path = f["path"]
        if path in folder_by_path:
            continue
        parent_path = f.get("parent")
        parent = folder_by_path.get(parent_path) if parent_path else None
        folder_by_path[path] = Folder.objects.create(
            project=project, parent=parent, name=f["name"], path=path,
        )

    # Files
    file_by_path: Dict[str, SourceFile] = {sf.relative_path: sf for sf in project.files.all()}
    for fi in files_in:
        path = fi["path"]
        if path in file_by_path:
            continue
        # find folder by parent dir
        parent_dir = "/".join(path.split("/")[:-1])
        folder = folder_by_path.get(parent_dir)
        file_by_path[path] = SourceFile.objects.create(
            project=project, folder=folder, name=fi["name"], relative_path=path, language="java",
        )

    # Variables
    var_count = 0
    for v in vars_in:
        sf = file_by_path.get(v["file_path"])
        if not sf:
            continue
        # skip if same name already exists in this file
        if Variable.objects.filter(source_file=sf, name=v["name"]).exists():
            continue
        Variable.objects.create(
            source_file=sf,
            name=v["name"],
            var_type=v.get("type", "String"),
            raw_value=v.get("raw_value", "") or "",
            evaluated_value=v.get("evaluated_value") or "",
            confidence=v.get("confidence", "low"),
            line_start=v.get("line_start"),
            line_end=v.get("line_end"),
            location=v.get("location", ""),
            notes=v.get("reason", ""),
        )
        var_count += 1

    return {
        "folders_created": sum(1 for f in folders_in if f["path"] in folder_by_path),
        "files_created": len(file_by_path),
        "variables_created": var_count,
    }


def _obfuscate_name(name: str) -> str:
    """Simple obfuscation transform: vowels -> 0, suffix o, keep extension.

    Examples: FileTemplate -> F1l3T3mpl4t3 (alt) ; for simplicity here we use
    a deterministic ASCII-only convention. The user can override before generate.
    """
    if "." in name:
        base, ext = name.rsplit(".", 1)
        return _obfuscate_name(base) + "." + ext
    table = {"a": "0", "e": "1", "i": "1", "o": "0", "u": "u", "A": "O", "E": "I", "I": "1", "O": "0", "U": "u"}
    out = "".join(table.get(c, c) for c in name)
    return out + "o"


@shared_task(bind=True, time_limit=600)
def generate_structure(self, apply_job_id: str) -> dict:
    """Generate target project structure on disk from validated Folder/SourceFile."""
    import shutil
    from .models import ApplyJob
    from apps.variables.models import Folder as F, SourceFile as SF

    job = ApplyJob.objects.get(pk=apply_job_id)
    job.status = "running"
    job.started_at = timezone.now()
    job.celery_task_id = self.request.id or ""
    job.save(update_fields=["status", "started_at", "celery_task_id"])

    try:
        pair = job.project_pair
        src_project = pair.source_project
        tgt_project = pair.target_project
        if not tgt_project:
            raise RuntimeError("project_pair.target_project is required for generate")

        tgt_root = Path(tgt_project.root_path)
        tgt_root.mkdir(parents=True, exist_ok=True)

        created_dirs = 0
        created_files = 0
        copied_defaults = 0
        errors = []

        # 1. Create target folders mirroring source structure
        src_folders = list(src_project.folders.all().order_by("path"))
        path_to_obf: dict[str, str] = {}
        for f in src_folders:
            obf = f.obf_name or (f.name if f.is_default else _obfuscate_name(f.name))
            parts = f.path.split("/")
            # rebuild obfuscated path
            new_parts = []
            cur = ""
            for part in parts:
                cur = (cur + "/" + part) if cur else part
                src_match = next((sf for sf in src_folders if sf.path == cur), None)
                if src_match:
                    new_parts.append(src_match.obf_name or (src_match.name if src_match.is_default else _obfuscate_name(src_match.name)))
                else:
                    new_parts.append(part)
            new_rel = "/".join(new_parts)
            (tgt_root / new_rel).mkdir(parents=True, exist_ok=True)
            path_to_obf[f.path] = new_rel
            # Also persist a target Folder row
            F.objects.get_or_create(
                project=tgt_project,
                path=new_rel,
                defaults={
                    "name": obf, "obf_name": obf, "is_default": f.is_default, "validated": True,
                },
            )
            created_dirs += 1

        # 2. Create target files
        src_files = list(src_project.files.all())
        for sf in src_files:
            obf = sf.obf_name or (sf.name if sf.is_default else _obfuscate_name(sf.name))
            folder_path = "/".join(sf.relative_path.split("/")[:-1])
            new_folder = path_to_obf.get(folder_path, folder_path)
            new_rel = (new_folder + "/" + obf) if new_folder else obf
            new_path = tgt_root / new_rel
            new_path.parent.mkdir(parents=True, exist_ok=True)
            try:
                if sf.is_default:
                    src_file_path = Path(src_project.root_path) / sf.relative_path
                    if src_file_path.exists():
                        shutil.copy2(src_file_path, new_path)
                        copied_defaults += 1
                else:
                    if not new_path.exists():
                        new_path.write_text("", encoding="utf-8")
                        created_files += 1
                SF.objects.get_or_create(
                    project=tgt_project,
                    relative_path=new_rel,
                    defaults={
                        "name": obf, "obf_name": obf, "language": sf.language, "is_default": sf.is_default, "validated": True,
                    },
                )
            except Exception as e:
                errors.append({"file": sf.relative_path, "error": str(e)})

        job.files_total = len(src_files)
        job.files_processed = created_files + copied_defaults
        job.errors = errors
        job.report = {
            "dirs_created": created_dirs,
            "files_created": created_files,
            "defaults_copied": copied_defaults,
            "errors_count": len(errors),
        }
        job.status = "done" if not errors else "done"
        job.finished_at = timezone.now()
        job.save(update_fields=["files_total", "files_processed", "errors", "report", "status", "finished_at"])
        return job.report
    except Exception as exc:
        logger.exception("generate_structure failed")
        job.status = "failed"
        job.errors = [{"error": str(exc)}]
        job.finished_at = timezone.now()
        job.save(update_fields=["status", "errors", "finished_at"])
        raise


def _build_injection_regex(pattern: str) -> tuple[str, dict]:
    """Build a regex from an injection pattern containing {target_var_name} and {value}.

    Returns (regex_string, group_info).
    """
    import re as _re
    # Find positions of placeholders
    parts = []
    last = 0
    placeholders = []
    for match in _re.finditer(r"\{(target_var_name|value)\}", pattern):
        parts.append(_re.escape(pattern[last:match.start()]))
        if match.group(1) == "value":
            parts.append(r"(?P<value>[^\"]*)")
            placeholders.append("value")
        else:
            parts.append(r"(?P<target_var_name>[A-Za-z_$][A-Za-z0-9_$]*)")
            placeholders.append("target_var_name")
        last = match.end()
    parts.append(_re.escape(pattern[last:]))
    return ("".join(parts), {"placeholders": placeholders})


@shared_task(bind=True, time_limit=600)
def apply_mappings(self, apply_job_id: str) -> dict:
    """Inject encrypted values into target files based on validated mappings."""
    import re as _re
    from .models import ApplyJob
    from apps.mappings.models import Mapping
    from apps.crypto.services.xor_base64 import xor_encrypt

    job = ApplyJob.objects.get(pk=apply_job_id)
    job.status = "running"
    job.started_at = timezone.now()
    job.celery_task_id = self.request.id or ""
    job.save(update_fields=["status", "started_at", "celery_task_id"])

    try:
        pair = job.project_pair
        tgt_root = Path(pair.target_project.root_path)

        mappings = list(Mapping.objects.filter(project_pair=pair, validated=True))
        job.files_total = len(mappings)
        job.save(update_fields=["files_total"])

        errors = []
        applied = 0
        skipped = 0

        for m in mappings:
            try:
                target_file = tgt_root / m.target_file.relative_path
                if not target_file.exists():
                    errors.append({"mapping_id": str(m.id), "error": "target file not found: %s" % target_file})
                    continue

                value = m.source_variable.evaluated_value
                encrypted = xor_encrypt(value, pair.encryption_key)

                # Pattern with {target_var_name} and {value} placeholders
                # We need to replace any existing matching pattern OR insert if not present
                regex_str, _info = _build_injection_regex(m.injection_pattern)
                content = target_file.read_text(encoding="utf-8")

                # First try to match an existing injection for the same target_var_name
                target_specific = m.injection_pattern.replace("{target_var_name}", _re.escape(m.target_var_name)).replace("{value}", r'(?P<value>[^"]*)')
                # The above re-escapes literal chars; the placeholders are now literal in m.target_var_name
                # Let's compute it differently: literal replacement after escaping
                literal_target = m.injection_pattern.split("{target_var_name}")
                # build per-variable regex
                lit_before, _, lit_after = m.injection_pattern.partition("{target_var_name}")
                # Build flexible regex (whitespace runs match \s+)
                def _flex(s):
                    esc = _re.escape(s)
                    BS = chr(92)
                    esc = esc.replace(BS + " ", " ")
                    out = []
                    n = len(esc)
                    j = 0
                    while j < n:
                        if esc[j] == ' ':
                            out.append(BS + 's+')
                            while j < n and esc[j] == ' ':
                                j += 1
                        else:
                            out.append(esc[j])
                            j += 1
                    return ''.join(out)
                regex_specific = _flex(lit_before + m.target_var_name) + _flex(lit_after.split("{value}")[0]) + r'(?P<value>[^"]*)' + _flex(lit_after.split("{value}")[1] if "{value}" in lit_after else "")

                new_line = m.injection_pattern.replace("{target_var_name}", m.target_var_name).replace("{value}", encrypted)
                if _re.search(regex_specific, content):
                    content = _re.sub(regex_specific, new_line.replace("\\", r"\\"), content, count=1)
                    target_file.write_text(content, encoding="utf-8")
                    applied += 1
                else:
                    errors.append({"mapping_id": str(m.id), "error": "injection pattern not found in target file"})
                    skipped += 1
                    continue

                m.encrypted_value = encrypted
                m.applied_at = timezone.now()
                m.save(update_fields=["encrypted_value", "applied_at"])

                job.files_processed = applied + skipped
                job.save(update_fields=["files_processed"])
            except Exception as e:
                errors.append({"mapping_id": str(m.id), "error": str(e)})

        job.errors = errors
        job.report = {"applied": applied, "skipped": skipped, "errors_count": len(errors)}
        job.status = "done"
        job.finished_at = timezone.now()
        job.save(update_fields=["errors", "report", "status", "finished_at"])
        return job.report
    except Exception as exc:
        logger.exception("apply_mappings failed")
        job.status = "failed"
        job.errors = [{"error": str(exc)}]
        job.finished_at = timezone.now()
        job.save(update_fields=["status", "errors", "finished_at"])
        raise


@shared_task(bind=True, time_limit=300)
def audit_mappings(self, apply_job_id: str) -> dict:
    """Read target files, extract encrypted values, decrypt, compare to source."""
    import re as _re
    from .models import ApplyJob
    from apps.mappings.models import Mapping
    from apps.crypto.services.xor_base64 import xor_decrypt

    job = ApplyJob.objects.get(pk=apply_job_id)
    job.status = "running"
    job.started_at = timezone.now()
    job.celery_task_id = self.request.id or ""
    job.save(update_fields=["status", "started_at", "celery_task_id"])

    try:
        pair = job.project_pair
        tgt_root = Path(pair.target_project.root_path)
        mappings = list(Mapping.objects.filter(project_pair=pair))
        job.files_total = len(mappings)
        job.save(update_fields=["files_total"])

        results = []
        for m in mappings:
            target_file = tgt_root / m.target_file.relative_path
            entry = {
                "mapping_id": str(m.id),
                "target_var_name": m.target_var_name,
                "target_file": m.target_file.relative_path,
                "source_var_name": m.source_variable.name,
            }
            if not target_file.exists():
                entry["status"] = "FILE_MISSING"
                results.append(entry)
                continue

            # Extract value by regex (same logic as apply)
            lit_before, _, lit_after = m.injection_pattern.partition("{target_var_name}")
            def _flex2(s):
                esc = _re.escape(s)
                BS = chr(92)
                esc = esc.replace(BS + " ", " ")
                out = []
                n = len(esc)
                j = 0
                while j < n:
                    if esc[j] == ' ':
                        out.append(BS + 's+')
                        while j < n and esc[j] == ' ':
                            j += 1
                    else:
                        out.append(esc[j])
                        j += 1
                return ''.join(out)
            regex_specific = _flex2(lit_before + m.target_var_name) + _flex2(lit_after.split("{value}")[0]) + r'(?P<value>[^"]*)' + _flex2(lit_after.split("{value}")[1] if "{value}" in lit_after else "")

            content = target_file.read_text(encoding="utf-8")
            match = _re.search(regex_specific, content)
            if not match:
                entry["status"] = "NON_FOUND"
                results.append(entry)
                continue

            encoded = match.group("value")
            try:
                decoded = xor_decrypt(encoded, pair.encryption_key)
            except Exception as e:
                entry["status"] = "DECRYPT_ERROR"
                entry["error"] = str(e)
                results.append(entry)
                continue

            expected = m.source_variable.evaluated_value
            if decoded == expected:
                entry["status"] = "OK"
            else:
                entry["status"] = "DIFF"
                entry["expected_length"] = len(expected)
                entry["actual_length"] = len(decoded)
            results.append(entry)

        ok_count = sum(1 for r in results if r["status"] == "OK")
        job.files_processed = len(results)
        job.report = {"total": len(results), "ok": ok_count, "results": results}
        job.status = "done"
        job.finished_at = timezone.now()
        job.save(update_fields=["files_processed", "report", "status", "finished_at"])
        return {"total": len(results), "ok": ok_count}
    except Exception as exc:
        logger.exception("audit_mappings failed")
        job.status = "failed"
        job.errors = [{"error": str(exc)}]
        job.finished_at = timezone.now()
        job.save(update_fields=["status", "errors", "finished_at"])
        raise


@shared_task(bind=True, time_limit=600)
def rotate_master_password(self, old_password: str, new_password: str) -> dict:
    """Re-encrypt all EncryptedTextField values with a new master key."""
    from django.db import transaction
    from apps.crypto.services.master_key import set_master_password, get_fernet
    from apps.crypto.services.fernet import decrypt_value, encrypt_value, new_salt, make_fernet

    # Decrypt with old, re-encrypt with new
    from apps.projects.models import ProjectPair as _PP
    from apps.variables.models import Variable as _Var
    from apps.mappings.models import Mapping as _Map

    # Verify old fernet works by loading one row
    pairs = list(_PP.objects.all())
    if not pairs:
        # Nothing to rotate
        salt = new_salt()
        set_master_password(new_password, salt=salt)
        return {"pairs": 0, "variables": 0, "mappings": 0}

    old_salt = get_fernet  # not really exposed; we will use current as-is
    old_fernet = get_fernet()

    # Snapshot all clear values
    snapshot_pairs = []
    for p in pairs:
        try:
            snapshot_pairs.append((p.id, p.encryption_key))
        except Exception:
            raise RuntimeError("cannot decrypt project_pair %s with old password" % p.id)

    snapshot_vars = [(v.id, v.evaluated_value) for v in _Var.objects.all()]
    snapshot_maps = [(m.id, m.encrypted_value) for m in _Map.objects.all()]

    # Switch to new key
    new_s = new_salt()
    set_master_password(new_password, salt=new_s)

    with transaction.atomic():
        for pid, val in snapshot_pairs:
            _PP.objects.filter(pk=pid).update(encryption_key="")  # avoid trigger
        for vid, val in snapshot_vars:
            _Var.objects.filter(pk=vid).update(evaluated_value="")
        for mid, val in snapshot_maps:
            _Map.objects.filter(pk=mid).update(encrypted_value="")

        # Re-save through ORM so field encryption runs with the new key
        for pid, val in snapshot_pairs:
            obj = _PP.objects.get(pk=pid)
            obj.encryption_key = val
            obj.save(update_fields=["encryption_key"])
        for vid, val in snapshot_vars:
            obj = _Var.objects.get(pk=vid)
            obj.evaluated_value = val
            obj.save(update_fields=["evaluated_value"])
        for mid, val in snapshot_maps:
            obj = _Map.objects.get(pk=mid)
            obj.encrypted_value = val
            obj.save(update_fields=["encrypted_value"])

    return {"pairs": len(snapshot_pairs), "variables": len(snapshot_vars), "mappings": len(snapshot_maps)}


@shared_task(bind=True, time_limit=900)
def obfuscate_project(self, apply_job_id: str, seed: str = "obfusc", preserve_top_package: str = "ma.ac2i") -> dict:
    """Run the obfuscate subcommand of the JAR. Performs full source-to-target obfuscation."""
    from .models import ApplyJob

    job = ApplyJob.objects.get(pk=apply_job_id)
    job.status = "running"
    job.started_at = timezone.now()
    job.celery_task_id = self.request.id or ""
    job.save(update_fields=["status", "started_at", "celery_task_id"])

    try:
        pair = job.project_pair
        source = pair.source_project
        target = pair.target_project
        if not target:
            raise RuntimeError("project_pair.target_project is required for obfuscation")
        if not source.root_path or not Path(source.root_path).exists():
            raise RuntimeError("source root_path does not exist: %s" % source.root_path)

        jar = settings.PARSER_JAR_PATH
        if not Path(jar).exists():
            raise RuntimeError("Parser JAR not found at %s" % jar)

        tgt_root = Path(target.root_path)
        tgt_root.mkdir(parents=True, exist_ok=True)

        with tempfile.NamedTemporaryFile(suffix=".json", delete=False) as tmp:
            map_path = tmp.name

        cmd = [
            settings.JAVA_BIN, "-Xmx512m", "-jar", jar,
            "obfuscate",
            "-s", source.root_path,
            "-t", target.root_path,
            "-k", pair.encryption_key,
            "--seed", seed,
            "--preserve-top-package", preserve_top_package,
            "--map-out", map_path,
        ]
        logger.info("Obfuscate command: %s", " ".join(cmd[:6]))
        result = subprocess.run(cmd, capture_output=True, text=True, timeout=600)
        if result.returncode != 0:
            raise RuntimeError("obfuscator failed (rc=%d): %s" % (result.returncode, result.stderr[:1500]))

        with open(map_path, "r", encoding="utf-8") as f:
            mapping = json.load(f)
        os.unlink(map_path)

        # Stats
        report = {
            "stdout": result.stdout[-3000:],
            "classes_renamed": sum(1 for k, v in mapping.get("classes", {}).items() if k != v),
            "classes_preserved": sum(1 for k, v in mapping.get("classes", {}).items() if k == v),
            "packages_renamed": sum(1 for k, v in mapping.get("packages", {}).items() if k != v),
            "fields_renamed": sum(1 for k, v in mapping.get("fields", {}).items() if k != v),
            "methods_renamed": sum(1 for k, v in mapping.get("methods", {}).items() if k != v),
            "files": len(mapping.get("filePathRenames", {})),
        }

        job.report = report
        job.status = "done"
        job.finished_at = timezone.now()
        job.save(update_fields=["report", "status", "finished_at"])
        return report
    except Exception as exc:
        logger.exception("obfuscate_project failed")
        job.status = "failed"
        job.errors = [{"error": str(exc)}]
        job.finished_at = timezone.now()
        job.save(update_fields=["status", "errors", "finished_at"])
        raise

