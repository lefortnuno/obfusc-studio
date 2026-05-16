"""AnalysisJob + ApplyJob models."""
from __future__ import annotations

import uuid
from django.db import models

from apps.projects.models import Project, ProjectPair


class AnalysisJob(models.Model):
    STATUSES = [("pending", "Pending"), ("running", "Running"), ("done", "Done"), ("failed", "Failed")]

    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    project = models.ForeignKey(Project, on_delete=models.CASCADE, related_name="analysis_jobs")
    status = models.CharField(max_length=20, choices=STATUSES, default="pending")
    celery_task_id = models.CharField(max_length=255, blank=True, default="")
    result_json = models.JSONField(null=True, blank=True)
    error_message = models.TextField(blank=True, default="")
    started_at = models.DateTimeField(null=True, blank=True)
    finished_at = models.DateTimeField(null=True, blank=True)
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        ordering = ["-created_at"]


class ApplyJob(models.Model):
    MODES = [("apply", "Apply"), ("generate", "Generate"), ("audit", "Audit"), ("obfuscate", "Obfuscate")]
    STATUSES = [("pending", "Pending"), ("running", "Running"), ("done", "Done"), ("failed", "Failed")]

    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    project_pair = models.ForeignKey(ProjectPair, on_delete=models.CASCADE, related_name="apply_jobs")
    mode = models.CharField(max_length=20, choices=MODES, default="apply")
    status = models.CharField(max_length=20, choices=STATUSES, default="pending")
    celery_task_id = models.CharField(max_length=255, blank=True, default="")
    files_processed = models.IntegerField(default=0)
    files_total = models.IntegerField(default=0)
    errors = models.JSONField(default=list, blank=True)
    report = models.JSONField(default=dict, blank=True)
    started_at = models.DateTimeField(null=True, blank=True)
    finished_at = models.DateTimeField(null=True, blank=True)
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        ordering = ["-created_at"]
