"""Full E2E pilot test: 8 validation criteria from AUTO_OBFUSCATION.md.

Pilot scenario:
- Source: Converter-unobf
- Target: temp copy of Converter-obf
- Key: A0x43x32x49$cwBJAQ==

Criteria:
1. Pair Converter-unobf -> tgt with the pilot key created
2. Analyze unobf -> 6 FileTemplate.java vars detected confidence=high
3. Validate 6 vars
4. Create 6 mappings to OoOo.java
5. Apply -> OoOo.java mutated
6. Audit -> 6 OK
"""
import re
import shutil
import pytest
from pathlib import Path

from apps.crypto.services.master_key import set_master_password, clear_master_password
from apps.crypto.services.xor_base64 import xor_decrypt, xor_encrypt
from apps.projects.models import Project, ProjectPair
from apps.variables.models import SourceFile, Variable
from apps.mappings.models import Mapping
from apps.jobs.models import AnalysisJob, ApplyJob
from apps.jobs.tasks import run_analysis, apply_mappings, audit_mappings


PILOT_KEY = "A0x43x32x49$cwBJAQ=="
PILOT_UNOBF = Path(r"C:\Users\rtoma\obfusc-studio\Converter-unobf")
PILOT_OBF = Path(r"C:\Users\rtoma\obfusc-studio\Converter-obf")
PARSER_JAR = Path(r"C:\Users\rtoma\obfusc-studio\obfuscmapper\parser\target\obfusc-parser.jar")

OoOo_REL = "src/main/java/ma/ac2i/y1r0/z1r0/c0o/OoOo.java"

# FileTemplate var name -> OoOo target field name
PILOT_MAPPING = {
    "xml_to_txt": "x0x5F111x5Fx116$",
    "xml_to_csv": "x0x5F111x5Fx118",
    "txt_to_xml": "t0x5F111x5Fx108",
    "txt_to_csv": "t0x5F111x5Fx118",
    "csv_to_xml": "c0x5F111x5Fx108",
    "csv_to_txt": "c0x5F111x5Fx116",
}


@pytest.fixture(autouse=True)
def master_key():
    set_master_password("test-master-password")
    yield
    clear_master_password()


@pytest.mark.django_db
@pytest.mark.skipif(not PILOT_UNOBF.exists() or not PILOT_OBF.exists() or not PARSER_JAR.exists(),
                    reason="Pilot artefacts missing")
def test_full_pilot_e2e(tmp_path, settings):
    settings.PARSER_JAR_PATH = str(PARSER_JAR)
    settings.JAVA_BIN = "C:/Program Files/Microsoft/jdk-17.0.19.10-hotspot/bin/java.exe"
    settings.CELERY_TASK_ALWAYS_EAGER = True

    # Prepare target tmp copy (don't touch the real Converter-obf)
    tgt_root = tmp_path / "tgt_obf"
    shutil.copytree(PILOT_OBF, tgt_root, ignore=shutil.ignore_patterns(".git"))

    # 1. Create pair
    src = Project.objects.create(name="Converter-unobf", root_path=str(PILOT_UNOBF))
    tgt = Project.objects.create(name="Converter-obf-tmp", root_path=str(tgt_root))
    pair = ProjectPair.objects.create(name="pilot", source_project=src, target_project=tgt, encryption_key=PILOT_KEY)

    # 2. Analyze source
    job = AnalysisJob.objects.create(project=src)
    run_analysis(str(job.id))
    job.refresh_from_db()
    assert job.status == "done", job.error_message

    ft_file = SourceFile.objects.get(project=src, name="FileTemplate.java")
    ft_vars = Variable.objects.filter(source_file=ft_file, name__in=PILOT_MAPPING.keys())
    assert ft_vars.count() == 6
    for v in ft_vars:
        assert v.confidence == "high"
        assert v.evaluated_value
        assert len(v.evaluated_value) > 100

    # 3. Validate
    ft_vars.update(validated=True)

    # 4. Create mappings (need to register target file)
    OoOo_file, _ = SourceFile.objects.get_or_create(
        project=tgt, relative_path=OoOo_REL,
        defaults={"name": "OoOo.java", "language": "java", "validated": True},
    )
    for src_name, tgt_name in PILOT_MAPPING.items():
        var = ft_vars.get(name=src_name)
        Mapping.objects.create(
            project_pair=pair,
            source_variable=var,
            target_file=OoOo_file,
            target_var_name=tgt_name,
            validated=True,
        )

    assert Mapping.objects.filter(project_pair=pair).count() == 6

    # 5. Apply
    apply_job = ApplyJob.objects.create(project_pair=pair, mode="apply")
    apply_mappings(str(apply_job.id))
    apply_job.refresh_from_db()
    assert apply_job.status == "done", apply_job.errors
    assert apply_job.report["applied"] == 6, apply_job.report

    # 6. Verify file content
    ooo_content = (tgt_root / OoOo_REL).read_text(encoding="utf-8")
    for src_name, tgt_name in PILOT_MAPPING.items():
        # Find encrypted value for that target var
        pattern = re.escape('this.' + tgt_name + ' = R04oo.d0x116_("') + r'([^"]+)' + re.escape('");')
        m = re.search(pattern, ooo_content)
        assert m, "injection line missing for " + tgt_name
        encoded = m.group(1)
        decoded = xor_decrypt(encoded, PILOT_KEY)
        expected = ft_vars.get(name=src_name).evaluated_value
        assert decoded == expected, "value mismatch for " + tgt_name

    # 7. Audit
    audit_job = ApplyJob.objects.create(project_pair=pair, mode="audit")
    audit_mappings(str(audit_job.id))
    audit_job.refresh_from_db()
    assert audit_job.status == "done", audit_job.errors
    assert audit_job.report["ok"] == 6, audit_job.report
    assert audit_job.report["total"] == 6

    # 8. Compile the mutated obf project (validation criterion 6)
    import subprocess as _sp
    import os as _os
    env = _os.environ.copy()
    env["JAVA_HOME"] = "C:/Program Files/Microsoft/jdk-17.0.19.10-hotspot"
    mvn_bin = "C:/Users/rtoma/tools/apache-maven-3.9.15/bin/mvn.cmd"
    if not Path(mvn_bin).exists():
        pytest.skip("mvn binary not present at " + mvn_bin)
    r = _sp.run([mvn_bin, "-q", "-DskipTests", "compile"], cwd=str(tgt_root),
                capture_output=True, text=True, timeout=600, env=env)
    assert r.returncode == 0, "mvn compile failed: " + r.stdout[-2000:] + " " + r.stderr[-2000:]
