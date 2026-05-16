"""Jobs API views: analysis trigger + status."""
from rest_framework import status, viewsets
from rest_framework.decorators import action
from rest_framework.response import Response

from apps.projects.models import Project, ProjectPair

from .models import AnalysisJob, ApplyJob
from .serializers import AnalysisJobSerializer, ApplyJobSerializer


class AnalysisJobViewSet(viewsets.ReadOnlyModelViewSet):
    queryset = AnalysisJob.objects.all()
    serializer_class = AnalysisJobSerializer


class ProjectAnalysisActionView(viewsets.ViewSet):
    """POST /api/projects/{id}/analyze/ -> creates and dispatches an analysis job."""
    pass


class ApplyJobViewSet(viewsets.ReadOnlyModelViewSet):
    queryset = ApplyJob.objects.all()
    serializer_class = ApplyJobSerializer
