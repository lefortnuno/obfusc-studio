"""Serializers for Folder + SourceFile + Variable."""
from rest_framework import serializers
from .models import Folder, SourceFile, Variable


class FolderSerializer(serializers.ModelSerializer):
    class Meta:
        model = Folder
        fields = ["id", "project", "parent", "name", "obf_name", "path", "is_default", "validated", "created_at"]
        read_only_fields = ["id", "created_at"]


class SourceFileSerializer(serializers.ModelSerializer):
    class Meta:
        model = SourceFile
        fields = ["id", "project", "folder", "name", "obf_name", "relative_path", "language", "is_default", "validated", "created_at", "updated_at"]
        read_only_fields = ["id", "created_at", "updated_at"]


class VariableSerializer(serializers.ModelSerializer):
    class Meta:
        model = Variable
        fields = [
            "id", "source_file", "name", "obf_name", "var_type", "raw_value", "evaluated_value",
            "is_sensitive", "confidence", "validated", "notes", "line_start", "line_end", "location",
            "created_at", "updated_at",
        ]
        read_only_fields = ["id", "created_at", "updated_at"]
        extra_kwargs = {"evaluated_value": {"write_only": True}}


class VariableReadSerializer(serializers.ModelSerializer):
    file_path = serializers.CharField(source="source_file.relative_path", read_only=True)

    class Meta:
        model = Variable
        fields = [
            "id", "source_file", "file_path", "name", "obf_name", "var_type", "raw_value",
            "is_sensitive", "confidence", "validated", "notes", "line_start", "line_end", "location",
            "created_at", "updated_at",
        ]
