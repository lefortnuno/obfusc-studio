"""Folder + SourceFile + Variable models."""
from __future__ import annotations

import uuid
from django.db import models

from apps.crypto.fields import EncryptedTextField
from apps.projects.models import Project


class Folder(models.Model):
    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    project = models.ForeignKey(Project, on_delete=models.CASCADE, related_name="folders")
    parent = models.ForeignKey("self", on_delete=models.CASCADE, null=True, blank=True, related_name="children")
    name = models.CharField(max_length=255)
    obf_name = models.CharField(max_length=255, blank=True, default="")
    path = models.CharField(max_length=1024)
    is_default = models.BooleanField(default=False)
    validated = models.BooleanField(default=False)
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        ordering = ["path"]
        unique_together = ("project", "path")

    def __str__(self) -> str:
        return self.path


class SourceFile(models.Model):
    LANGUAGES = [("java", "Java"), ("other", "Other")]

    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    project = models.ForeignKey(Project, on_delete=models.CASCADE, related_name="files")
    folder = models.ForeignKey(Folder, on_delete=models.CASCADE, related_name="files", null=True, blank=True)
    name = models.CharField(max_length=255)
    obf_name = models.CharField(max_length=255, blank=True, default="")
    relative_path = models.CharField(max_length=1024)
    language = models.CharField(max_length=50, choices=LANGUAGES, default="java")
    is_default = models.BooleanField(default=False)
    validated = models.BooleanField(default=False)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        ordering = ["relative_path"]
        unique_together = ("project", "relative_path")

    def __str__(self) -> str:
        return self.relative_path


class Variable(models.Model):
    CONFIDENCES = [("high", "High"), ("medium", "Medium"), ("low", "Low"), ("manual", "Manual")]

    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    source_file = models.ForeignKey(SourceFile, on_delete=models.CASCADE, related_name="variables")
    name = models.CharField(max_length=255)
    obf_name = models.CharField(max_length=255, blank=True, default="")
    var_type = models.CharField(max_length=100, default="String")
    raw_value = models.TextField(blank=True, default="")
    evaluated_value = EncryptedTextField(blank=True, default="")
    is_sensitive = models.BooleanField(default=True)
    confidence = models.CharField(max_length=20, choices=CONFIDENCES, default="manual")
    validated = models.BooleanField(default=False)
    notes = models.TextField(blank=True, default="")
    line_start = models.IntegerField(null=True, blank=True)
    line_end = models.IntegerField(null=True, blank=True)
    location = models.CharField(max_length=50, blank=True, default="")
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        ordering = ["name"]

    def __str__(self) -> str:
        return self.name
