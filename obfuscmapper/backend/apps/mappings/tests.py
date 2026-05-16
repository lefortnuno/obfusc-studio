"""Mapping CRUD + preview test."""
import pytest
from pathlib import Path

from apps.crypto.services.master_key import set_master_password, clear_master_password
from apps.crypto.services.xor_base64 import xor_decrypt
from apps.projects.models import Project, ProjectPair
from apps.variables.models import SourceFile, Variable
from apps.mappings.models import Mapping


@pytest.fixture(autouse=True)
def master_key():
    set_master_password("test-master-password")
    yield
    clear_master_password()


@pytest.fixture
def pilot_pair(tmp_path: Path):
    src = Project.objects.create(name="src", root_path=str(tmp_path))
    tgt = Project.objects.create(name="tgt", root_path=str(tmp_path))
    pair = ProjectPair.objects.create(
        name="pair", source_project=src, target_project=tgt,
        encryption_key="A0x43x32x49$cwBJAQ==",
    )
    return pair


@pytest.mark.django_db
def test_create_mapping_and_preview(pilot_pair):
    from rest_framework.test import APIClient
    src_file = SourceFile.objects.create(project=pilot_pair.source_project, name="A.java", relative_path="A.java")
    src_var = Variable.objects.create(source_file=src_file, name="xml_to_txt", evaluated_value="ADMIN", confidence="high", validated=True)
    tgt_file = SourceFile.objects.create(project=pilot_pair.target_project, name="OoOo.java", relative_path="OoOo.java")
    c = APIClient()
    r = c.post("/api/mappings/", {
        "project_pair": str(pilot_pair.id),
        "source_variable": str(src_var.id),
        "target_file": str(tgt_file.id),
        "target_var_name": "x0x5F111x5Fx116$",
    }, format="json")
    assert r.status_code == 201, r.json()
    mid = r.json()["id"]

    r = c.post("/api/mappings/%s/preview/" % mid, {}, format="json")
    assert r.status_code == 200, r.json()
    data = r.json()
    assert data["verify_ok"] is True
    assert "x0x5F111x5Fx116$" in data["injection_line_preview"]
    # AHQ1fX0= is the ciphertext of ADMIN
    assert "AHQ1fX0=" in data["injection_line_preview"]
