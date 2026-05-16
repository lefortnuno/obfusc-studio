"""Serializers for Project + ProjectPair."""
from __future__ import annotations

from rest_framework import serializers

from .models import Project, ProjectPair


class ProjectSerializer(serializers.ModelSerializer):
    class Meta:
        model = Project
        fields = ["id", "name", "description", "root_path", "language", "project_type", "created_at", "updated_at"]
        read_only_fields = ["id", "created_at", "updated_at"]

    def validate_root_path(self, value: str) -> str:
        from .models import validate_root_path
        validate_root_path(value)
        return value


class ProjectPairReadSerializer(serializers.ModelSerializer):
    source_project_name = serializers.CharField(source="source_project.name", read_only=True)
    target_project_name = serializers.CharField(source="target_project.name", read_only=True, default=None)
    encryption_key = serializers.SerializerMethodField()

    class Meta:
        model = ProjectPair
        fields = [
            "id", "name",
            "source_project", "source_project_name",
            "target_project", "target_project_name",
            "encryption_key", "algorithm",
            "created_at", "updated_at",
        ]

    def get_encryption_key(self, obj: ProjectPair) -> str:
        return "****" if obj.encryption_key else ""


class ProjectPairWriteSerializer(serializers.ModelSerializer):
    class Meta:
        model = ProjectPair
        fields = ["id", "name", "source_project", "target_project", "encryption_key", "algorithm"]
        read_only_fields = ["id"]

    def validate_encryption_key(self, value: str) -> str:
        if not value:
            raise serializers.ValidationError("Encryption key is required.")
        if len(value) < 4:
            raise serializers.ValidationError("Encryption key too short (min 4 chars).")
        return value
