"""Project + ProjectPair models."""
from __future__ import annotations

import uuid
from pathlib import Path

from django.core.exceptions import ValidationError
from django.db import models

from apps.crypto.fields import EncryptedTextField


def validate_root_path(value: str) -> None:
    if not value:
        return
    p = Path(value)
    if not p.exists():
        raise ValidationError("root_path does not exist: %s" % value)
    if not p.is_dir():
        raise ValidationError("root_path is not a directory: %s" % value)


class Project(models.Model):
    LANGUAGES = [("java", "Java")]
    TYPES = [("monolith", "Monolith"), ("microservice", "Microservice")]

    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    name = models.CharField(max_length=255, unique=True)
    description = models.TextField(blank=True, default="")
    root_path = models.CharField(max_length=1024)
    language = models.CharField(max_length=50, choices=LANGUAGES, default="java")
    project_type = models.CharField(max_length=50, choices=TYPES, default="monolith")
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        ordering = ["name"]

    def clean(self):
        super().clean()
        validate_root_path(self.root_path)

    def __str__(self) -> str:
        return self.name


class ProjectPair(models.Model):
    ALGORITHMS = [("xor_base64", "XOR + Base64")]

    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    name = models.CharField(max_length=255)
    source_project = models.ForeignKey(
        Project, on_delete=models.CASCADE, related_name="pairs_as_source"
    )
    target_project = models.ForeignKey(
        Project, on_delete=models.SET_NULL, related_name="pairs_as_target",
        null=True, blank=True,
    )
    encryption_key = EncryptedTextField()
    algorithm = models.CharField(max_length=50, choices=ALGORITHMS, default="xor_base64")
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        ordering = ["name"]

    def __str__(self) -> str:
        return "%s (%s -> %s)" % (self.name, self.source_project_id, self.target_project_id)
