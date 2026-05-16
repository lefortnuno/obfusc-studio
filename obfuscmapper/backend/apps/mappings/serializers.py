from rest_framework import serializers
from .models import Mapping


class MappingSerializer(serializers.ModelSerializer):
    source_variable_name = serializers.CharField(source="source_variable.name", read_only=True)
    source_file_path = serializers.CharField(source="source_variable.source_file.relative_path", read_only=True)
    target_file_path = serializers.CharField(source="target_file.relative_path", read_only=True)

    class Meta:
        model = Mapping
        fields = [
            "id", "project_pair", "source_variable", "source_variable_name", "source_file_path",
            "target_file", "target_file_path", "target_var_name",
            "injection_pattern", "validated", "applied_at",
            "created_at", "updated_at",
        ]
        read_only_fields = ["id", "applied_at", "created_at", "updated_at"]
