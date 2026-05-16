"""End-to-end pilot test: F-APPLY injects encrypted values into OoOo.java copy."""
import shutil
import pytest
from pathlib import Path

from apps.crypto.services.master_key import set_master_password, clear_master_password
from apps.crypto.services.xor_base64 import xor_decrypt, xor_encrypt
from apps.projects.models import Project, ProjectPair
from apps.variables.models import SourceFile, Variable
from apps.mappings.models import Mapping
from apps.jobs.models import ApplyJob
from apps.jobs.tasks import apply_mappings


PILOT_KEY = "A0x43x32x49$cwBJAQ=="
SRC_OoOo = Path(r"C:\Users\rtoma\obfusc-studio\Converter-obf\src\main\java\ma\ac2i\y1r0\z1r0\c0o\OoOo.java")


@pytest.fixture(autouse=True)
def master_key():
    set_master_password("test-master-password")
    yield
    clear_master_password()


@pytest.mark.django_db
@pytest.mark.skipif(not SRC_OoOo.exists(), reason="Pilot OoOo.java missing")
def test_apply_pilot_injection(tmp_path: Path):
    """Copy OoOo.java to tmp, set up source var, run apply, verify injected encrypted value."""
    # Set up target project pointing to tmp dir
    src_proj_dir = tmp_path / "src_proj"
    tgt_proj_dir = tmp_path / "tgt_proj"
    src_proj_dir.mkdir()
    tgt_proj_dir.mkdir()

    # Mirror the path structure expected by mapping
    rel_path = "src/main/java/ma/ac2i/y1r0/z1r0/c0o/OoOo.java"
    target_file = tgt_proj_dir / rel_path
    target_file.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(SRC_OoOo, target_file)

    src_proj = Project.objects.create(name="src", root_path=str(src_proj_dir))
    tgt_proj = Project.objects.create(name="tgt", root_path=str(tgt_proj_dir))
    pair = ProjectPair.objects.create(
        name="pilot", source_project=src_proj, target_project=tgt_proj, encryption_key=PILOT_KEY,
    )

    # Create source variable (the value to inject) - take a fresh value, not the original
    src_file = SourceFile.objects.create(project=src_proj, name="A.java", relative_path="A.java")
    new_value = "MY_FRESH_INJECTED_VALUE_42"
    var = Variable.objects.create(
        source_file=src_file, name="xml_to_txt", evaluated_value=new_value,
        confidence="high", validated=True,
    )

    # Target file row
    tgt_file = SourceFile.objects.create(
        project=tgt_proj, name="OoOo.java", relative_path=rel_path,
    )

    # Create a mapping for x0x5F111x5Fx116$ (xml_to_txt in OoOo.java)
    m = Mapping.objects.create(
        project_pair=pair,
        source_variable=var,
        target_file=tgt_file,
        target_var_name="x0x5F111x5Fx116$",
        validated=True,
    )

    # Run apply
    job = ApplyJob.objects.create(project_pair=pair, mode="apply")
    apply_mappings(str(job.id))

    job.refresh_from_db()
    assert job.status == "done", job.errors
    assert job.report.get("applied") == 1, job.report

    # Read the file and verify the encrypted value
    content = target_file.read_text(encoding="utf-8")
    expected_encrypted = xor_encrypt(new_value, PILOT_KEY)
    assert expected_encrypted in content

    # Symmetric check: decrypt the value found should equal the new_value
    import re
    match = re.search(r'this\.x0x5F111x5Fx116\$ = R04oo\.d0x116_\("([^"]+)"\);', content)
    assert match, "injection line not found post-apply"
    assert xor_decrypt(match.group(1), PILOT_KEY) == new_value
