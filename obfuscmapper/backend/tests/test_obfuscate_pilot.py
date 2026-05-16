"""E2E test: obfuscate Converter-unobf -> tmp -> mvn compile passes."""
import os
import pytest
import subprocess
from pathlib import Path

from apps.crypto.services.master_key import set_master_password, clear_master_password
from apps.projects.models import Project, ProjectPair
from apps.jobs.models import ApplyJob
from apps.jobs.tasks import obfuscate_project


PILOT_KEY = "A0x43x32x49$cwBJAQ=="
PILOT_UNOBF = Path(r"C:\Users\rtoma\obfusc-studio\Converter-unobf")
PARSER_JAR = Path(r"C:\Users\rtoma\obfusc-studio\obfuscmapper\parser\target\obfusc-parser.jar")
JAVA_BIN = "C:/Program Files/Microsoft/jdk-17.0.19.10-hotspot/bin/java.exe"
MVN_BIN = r"C:\Users\rtoma\tools\apache-maven-3.9.15\bin\mvn.cmd"


@pytest.fixture(autouse=True)
def master_key():
    set_master_password("test-master-password")
    yield
    clear_master_password()


@pytest.mark.django_db
@pytest.mark.skipif(not PILOT_UNOBF.exists(), reason="Pilot unobf missing")
@pytest.mark.skipif(not PARSER_JAR.exists(), reason="Parser JAR missing")
def test_full_obfuscation_compiles(settings, tmp_path):
    settings.PARSER_JAR_PATH = str(PARSER_JAR)
    settings.JAVA_BIN = JAVA_BIN
    settings.CELERY_TASK_ALWAYS_EAGER = True

    src = Project.objects.create(name="Converter-unobf", root_path=str(PILOT_UNOBF))
    tgt_root = tmp_path / "obf"
    tgt_root.mkdir()
    tgt = Project.objects.create(name="Converter-obf-tmp", root_path=str(tgt_root))
    pair = ProjectPair.objects.create(name="pilote", source_project=src, target_project=tgt, encryption_key=PILOT_KEY)

    job = ApplyJob.objects.create(project_pair=pair, mode="obfuscate")
    obfuscate_project(str(job.id))
    job.refresh_from_db()
    assert job.status == "done", job.errors
    assert (job.report or {}).get("classes_renamed", 0) > 5
    assert (job.report or {}).get("packages_renamed", 0) >= 1

    if not Path(MVN_BIN).exists():
        pytest.skip("mvn not present at " + MVN_BIN)
    env = os.environ.copy()
    env["JAVA_HOME"] = "C:/Program Files/Microsoft/jdk-17.0.19.10-hotspot"
    r = subprocess.run(
        [MVN_BIN, "-q", "-DskipTests", "compile"],
        cwd=str(tgt_root), capture_output=True, text=True, timeout=600, env=env,
    )
    assert r.returncode == 0, "mvn compile failed:\nstdout:\n" + (r.stdout[-2000:] if r.stdout else "") + "\nstderr:\n" + (r.stderr[-2000:] if r.stderr else "")
