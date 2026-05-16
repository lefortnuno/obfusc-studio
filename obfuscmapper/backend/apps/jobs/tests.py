"""F-ANALYZE integration test: run JAR on Converter-unobf and ingest results."""
import os
import pytest
from pathlib import Path

from apps.crypto.services.master_key import set_master_password, clear_master_password
from apps.projects.models import Project
from apps.variables.models import Variable, SourceFile
from apps.jobs.models import AnalysisJob
from apps.jobs.tasks import run_analysis


PILOT_UNOBF = r"C:\Users\rtoma\obfusc-studio\Converter-unobf"
PARSER_JAR = r"C:\Users\rtoma\obfusc-studio\obfuscmapper\parser\target\obfusc-parser.jar"


@pytest.fixture(autouse=True)
def master_key():
    set_master_password("test-master-password")
    yield
    clear_master_password()


@pytest.mark.django_db
@pytest.mark.skipif(not Path(PILOT_UNOBF).exists(), reason="Pilot unobf project not present")
@pytest.mark.skipif(not Path(PARSER_JAR).exists(), reason="Parser JAR not built")
def test_analyze_pilot_finds_filetemplate_vars(settings):
    settings.PARSER_JAR_PATH = PARSER_JAR
    settings.JAVA_BIN = "C:/Program Files/Microsoft/jdk-17.0.19.10-hotspot/bin/java.exe"
    settings.CELERY_TASK_ALWAYS_EAGER = True

    project = Project.objects.create(name="Converter-unobf", root_path=PILOT_UNOBF)
    job = AnalysisJob.objects.create(project=project)

    run_analysis(str(job.id))

    job.refresh_from_db()
    assert job.status == "done", job.error_message

    ft_files = SourceFile.objects.filter(project=project, name="FileTemplate.java")
    assert ft_files.count() == 1
    ft = ft_files.first()

    vars = Variable.objects.filter(source_file=ft)
    names = sorted(v.name for v in vars)
    assert set(names) >= {"xml_to_txt", "xml_to_csv", "txt_to_xml", "txt_to_csv", "csv_to_xml", "csv_to_txt"}, names

    # all should be high
    for v in vars:
        if v.name in {"xml_to_txt", "xml_to_csv", "txt_to_xml", "txt_to_csv", "csv_to_xml", "csv_to_txt"}:
            assert v.confidence == "high", (v.name, v.confidence)
            assert v.evaluated_value, v.name
            assert len(v.evaluated_value) > 100, (v.name, len(v.evaluated_value))
