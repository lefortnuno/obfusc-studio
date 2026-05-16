"""Tests for Project + ProjectPair CRUD."""
import os
import pytest
from pathlib import Path
from rest_framework.test import APIClient

from apps.crypto.services.master_key import set_master_password, clear_master_password
from apps.projects.models import Project, ProjectPair


@pytest.fixture(autouse=True)
def master_key():
    set_master_password("test-master-password")
    yield
    clear_master_password()


@pytest.fixture
def existing_dir(tmp_path: Path) -> str:
    return str(tmp_path)


@pytest.mark.django_db
def test_create_project(existing_dir):
    c = APIClient()
    r = c.post("/api/projects/", {"name": "p1", "root_path": existing_dir}, format="json")
    assert r.status_code == 201, r.json()
    assert r.json()["name"] == "p1"


@pytest.mark.django_db
def test_create_project_rejects_missing_path():
    c = APIClient()
    r = c.post("/api/projects/", {"name": "p1", "root_path": "/nope/does/not/exist"}, format="json")
    assert r.status_code == 400


@pytest.mark.django_db
def test_pair_key_is_masked_in_read(existing_dir):
    p1 = Project.objects.create(name="src", root_path=existing_dir)
    p2 = Project.objects.create(name="dst", root_path=existing_dir)
    c = APIClient()
    r = c.post("/api/project-pairs/", {
        "name": "pair1", "source_project": str(p1.id), "target_project": str(p2.id),
        "encryption_key": "A0x43x32x49$cwBJAQ==",
    }, format="json")
    assert r.status_code == 201, r.json()
    pair_id = r.json()["id"]
    assert r.json()["encryption_key"] == "****"

    # Get
    r = c.get("/api/project-pairs/%s/" % pair_id)
    assert r.json()["encryption_key"] == "****"


@pytest.mark.django_db
def test_pair_key_is_encrypted_in_db(existing_dir):
    p1 = Project.objects.create(name="src", root_path=existing_dir)
    pair = ProjectPair.objects.create(
        name="x",
        source_project=p1,
        encryption_key="A0x43x32x49$cwBJAQ==",
    )

    # Read raw column from DB to ensure not stored in clear
    from django.db import connection
    with connection.cursor() as cur:
        cur.execute("SELECT encryption_key FROM projects_projectpair")
        rows = cur.fetchall()
        raw = rows[0][0]
    assert raw != "A0x43x32x49$cwBJAQ=="
    assert "A0x43" not in raw

    # And the ORM round-trip yields the original value
    refreshed = ProjectPair.objects.get(pk=pair.pk)
    assert refreshed.encryption_key == "A0x43x32x49$cwBJAQ=="


@pytest.mark.django_db
def test_pair_requires_encryption_key(existing_dir):
    p1 = Project.objects.create(name="src", root_path=existing_dir)
    c = APIClient()
    r = c.post("/api/project-pairs/", {
        "name": "x", "source_project": str(p1.id), "encryption_key": "",
    }, format="json")
    assert r.status_code == 400
