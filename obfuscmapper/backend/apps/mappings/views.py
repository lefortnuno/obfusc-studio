from rest_framework import viewsets
from rest_framework.decorators import action
from rest_framework.response import Response

from apps.crypto.services.xor_base64 import xor_encrypt, xor_decrypt
from apps.projects.models import ProjectPair

from .models import Mapping
from .serializers import MappingSerializer


class MappingViewSet(viewsets.ModelViewSet):
    queryset = Mapping.objects.select_related("source_variable", "source_variable__source_file", "target_file", "project_pair").all()
    serializer_class = MappingSerializer

    @action(detail=True, methods=["post"], url_path="preview")
    def preview(self, request, pk=None):
        mapping = self.get_object()
        value = mapping.source_variable.evaluated_value
        pair = mapping.project_pair
        encrypted = xor_encrypt(value, pair.encryption_key)
        verify_ok = xor_decrypt(encrypted, pair.encryption_key) == value
        injection_line = mapping.injection_pattern.replace("{target_var_name}", mapping.target_var_name).replace("{value}", encrypted)
        return Response({
            "encrypted_length": len(encrypted),
            "verify_ok": verify_ok,
            "injection_line_preview": injection_line[:200] + ("..." if len(injection_line) > 200 else ""),
        })
