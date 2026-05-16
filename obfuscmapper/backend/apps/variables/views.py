"""Folder + SourceFile + Variable + tree + preview-encryption."""
from rest_framework import status, viewsets
from rest_framework.decorators import action
from rest_framework.response import Response

from apps.crypto.services.master_key import get_fernet
from apps.crypto.services.fernet import decrypt_value
from apps.crypto.services.xor_base64 import xor_encrypt, xor_decrypt
from apps.projects.models import Project, ProjectPair

from .models import Folder, SourceFile, Variable
from .serializers import (
    FolderSerializer,
    SourceFileSerializer,
    VariableSerializer,
    VariableReadSerializer,
)


DEFAULT_NAMES = {
    "pom.xml", "build.gradle", "build.gradle.kts", "settings.gradle", "application.properties",
    "application.yml", "application.yaml", "mvnw", "mvnw.cmd", "gradlew", "gradlew.bat",
    ".gitignore", "README.md",
}
DEFAULT_FOLDERS = {"resources", "static", "templates", "META-INF"}


class FolderViewSet(viewsets.ModelViewSet):
    queryset = Folder.objects.all()
    serializer_class = FolderSerializer

    @action(detail=True, methods=["post"], url_path="mark-defaults")
    def mark_defaults(self, request, pk=None):
        root = self.get_object()
        marked = 0
        # Mark this and all descendants if names match defaults
        for folder in [root, *root.children.all()]:
            if folder.name in DEFAULT_FOLDERS:
                folder.is_default = True
                folder.save(update_fields=["is_default"])
                marked += 1
        files_marked = 0
        for sf in SourceFile.objects.filter(project=root.project, relative_path__startswith=root.path):
            if sf.name in DEFAULT_NAMES:
                sf.is_default = True
                sf.save(update_fields=["is_default"])
                files_marked += 1
        return Response({"folders_marked": marked, "files_marked": files_marked})


class SourceFileViewSet(viewsets.ModelViewSet):
    queryset = SourceFile.objects.all()
    serializer_class = SourceFileSerializer


class ProjectTreeView(viewsets.ViewSet):
    """GET /api/projects/{id}/tree/ -> nested folder + files tree."""
    pass


class VariableViewSet(viewsets.ModelViewSet):
    queryset = Variable.objects.all()
    serializer_class = VariableSerializer

    def get_serializer_class(self):
        if self.action in ("list", "retrieve"):
            return VariableReadSerializer
        return VariableSerializer

    @action(detail=False, methods=["post"], url_path="preview-encryption")
    def preview_encryption(self, request):
        value = request.data.get("value", "")
        pair_id = request.data.get("project_pair_id")
        if not pair_id:
            return Response({"detail": "project_pair_id required"}, status=400)
        try:
            pair = ProjectPair.objects.get(pk=pair_id)
        except ProjectPair.DoesNotExist:
            return Response({"detail": "pair not found"}, status=404)
        encrypted = xor_encrypt(value, pair.encryption_key)
        decrypted = xor_decrypt(encrypted, pair.encryption_key)
        return Response({"encrypted": encrypted, "verify_ok": decrypted == value, "length": len(encrypted)})
    @action(detail=False, methods=["post"], url_path="bulk-validate")
    def bulk_validate(self, request):
        ids = request.data.get("ids") or []
        action_name = request.data.get("action", "validate")
        qs = Variable.objects.filter(id__in=ids)
        if action_name == "validate":
            n = qs.update(validated=True)
        elif action_name == "unsensitive":
            n = qs.update(is_sensitive=False, validated=True)
        elif action_name == "delete":
            n, _ = qs.delete()
        else:
            return Response({"detail": "unknown action"}, status=400)
        return Response({"updated": n, "action": action_name})

