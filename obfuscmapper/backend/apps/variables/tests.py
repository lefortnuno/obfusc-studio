"""Tests F-TREE + F-VAR (Folder/SourceFile/Variable + preview)."""
import pytest
from rest_framework.test import APIClient
from pathlib import Path

from apps.crypto.services.master_key import set_master_password, clear_master_password
from apps.crypto.services.xor_base64 import xor_decrypt
from apps.projects.models import Project, ProjectPair
from apps.variables.models import Folder, SourceFile, Variable


@pytest.fixture(autouse=True)
def master_key():
    set_master_password("test-master-password")
    yield
    clear_master_password()


@pytest.fixture
def project(tmp_path: Path) -> Project:
    return Project.objects.create(name="p1", root_path=str(tmp_path))


@pytest.mark.django_db
def test_create_folder(project):
    c = APIClient()
    r = c.post("/api/folders/", {
        "project": str(project.id), "name": "src", "path": "src",
    }, format="json")
    assert r.status_code == 201, r.json()


@pytest.mark.django_db
def test_tree_endpoint_nested(project):
    src = Folder.objects.create(project=project, name="src", path="src")
    main = Folder.objects.create(project=project, parent=src, name="main", path="src/main")
    SourceFile.objects.create(project=project, folder=main, name="A.java", relative_path="src/main/A.java")
    SourceFile.objects.create(project=project, folder=src, name="B.java", relative_path="src/B.java")

    c = APIClient()
    r = c.get("/api/projects/%s/tree/" % project.id)
    assert r.status_code == 200
    data = r.json()
    assert data["name"] == "p1"
    # root has src, src has main + B.java
    assert len(data["tree"]) >= 1


@pytest.mark.django_db
def test_variable_preview_encryption(project):
    pair = ProjectPair.objects.create(
        name="pair", source_project=project, encryption_key="A0x43x32x49$cwBJAQ==",
    )
    c = APIClient()
    r = c.post("/api/variables/preview-encryption/", {
        "value": "ADMIN", "project_pair_id": str(pair.id),
    }, format="json")
    assert r.status_code == 200, r.json()
    data = r.json()
    assert data["verify_ok"] is True
    # Pre-computed: encrypt("ADMIN", pilot key) == "AHQ1fX0="
    assert data["encrypted"] == "AHQ1fX0="


@pytest.mark.django_db
def test_variable_encrypted_in_db(project):
    sf = SourceFile.objects.create(project=project, name="A.java", relative_path="A.java")
    v = Variable.objects.create(source_file=sf, name="X", evaluated_value="ADMIN")
    from django.db import connection
    with connection.cursor() as cur:
        cur.execute("SELECT evaluated_value FROM variables_variable")
        rows = cur.fetchall()
    raw = rows[0][0]
    assert raw != "ADMIN"
    refreshed = Variable.objects.get(pk=v.pk)
    assert refreshed.evaluated_value == "ADMIN"
