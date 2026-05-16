"""ViewSets for Project + ProjectPair."""
from __future__ import annotations

from rest_framework import status, viewsets
from rest_framework.response import Response

from .models import Project, ProjectPair
from .serializers import (
    ProjectSerializer,
    ProjectPairReadSerializer,
    ProjectPairWriteSerializer,
)


class ProjectViewSet(viewsets.ModelViewSet):
    queryset = Project.objects.all()
    serializer_class = ProjectSerializer


class ProjectPairViewSet(viewsets.ModelViewSet):
    queryset = ProjectPair.objects.select_related("source_project", "target_project").all()

    def get_serializer_class(self):
        if self.action in ("list", "retrieve"):
            return ProjectPairReadSerializer
        return ProjectPairWriteSerializer

    def create(self, request, *args, **kwargs):
        ser = ProjectPairWriteSerializer(data=request.data)
        ser.is_valid(raise_exception=True)
        instance = ser.save()
        out = ProjectPairReadSerializer(instance)
        return Response(out.data, status=status.HTTP_201_CREATED)

    def update(self, request, *args, **kwargs):
        instance = self.get_object()
        partial = kwargs.pop("partial", False)
        ser = ProjectPairWriteSerializer(instance, data=request.data, partial=partial)
        ser.is_valid(raise_exception=True)
        instance = ser.save()
        out = ProjectPairReadSerializer(instance)
        return Response(out.data)
