"""Mapping model."""
from __future__ import annotations

import uuid
from django.db import models

from apps.crypto.fields import EncryptedTextField
from apps.projects.models import ProjectPair
from apps.variables.models import SourceFile, Variable


DEFAULT_PATTERN = 'this.{target_var_name} = R04oo.d0x116_("{value}");'


class Mapping(models.Model):
    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    project_pair = models.ForeignKey(ProjectPair, on_delete=models.CASCADE, related_name="mappings")
    source_variable = models.ForeignKey(Variable, on_delete=models.CASCADE, related_name="mappings")
    target_file = models.ForeignKey(SourceFile, on_delete=models.CASCADE, related_name="incoming_mappings")
    target_var_name = models.CharField(max_length=255)
    encrypted_value = EncryptedTextField(blank=True, default="")
    injection_pattern = models.TextField(default=DEFAULT_PATTERN)
    validated = models.BooleanField(default=False)
    applied_at = models.DateTimeField(null=True, blank=True)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        ordering = ["target_var_name"]
