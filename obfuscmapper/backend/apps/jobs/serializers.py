from rest_framework import serializers
from .models import AnalysisJob, ApplyJob


class AnalysisJobSerializer(serializers.ModelSerializer):
    project_name = serializers.CharField(source="project.name", read_only=True)

    class Meta:
        model = AnalysisJob
        fields = ["id", "project", "project_name", "status", "celery_task_id", "error_message", "result_json", "started_at", "finished_at", "created_at"]
        read_only_fields = fields


class ApplyJobSerializer(serializers.ModelSerializer):
    pair_name = serializers.CharField(source="project_pair.name", read_only=True)

    class Meta:
        model = ApplyJob
        fields = ["id", "project_pair", "pair_name", "mode", "status", "celery_task_id", "files_processed", "files_total", "errors", "report", "started_at", "finished_at", "created_at"]
        read_only_fields = fields
