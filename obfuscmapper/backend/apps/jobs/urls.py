from django.urls import path
from rest_framework.decorators import api_view
from rest_framework.response import Response
from rest_framework.routers import DefaultRouter

from apps.projects.models import Project, ProjectPair

from .models import AnalysisJob, ApplyJob
from .serializers import AnalysisJobSerializer, ApplyJobSerializer
from .views import AnalysisJobViewSet, ApplyJobViewSet
from .tasks import run_analysis


router = DefaultRouter()
router.register(r"analysis-jobs", AnalysisJobViewSet)
router.register(r"apply-jobs", ApplyJobViewSet)


@api_view(["POST"])
def trigger_analysis(request, project_id):
    try:
        project = Project.objects.get(pk=project_id)
    except Project.DoesNotExist:
        return Response({"detail": "not found"}, status=404)
    job = AnalysisJob.objects.create(project=project)
    async_result = run_analysis.delay(str(job.id))
    job.celery_task_id = async_result.id or ""
    job.save(update_fields=["celery_task_id"])
    return Response(AnalysisJobSerializer(job).data, status=202)


urlpatterns = router.urls + [
    path("projects/<uuid:project_id>/analyze/", trigger_analysis, name="trigger-analysis"),
]


from .tasks import generate_structure as _generate_structure


@api_view(["POST"])
def trigger_generate(request, pair_id):
    try:
        pair = ProjectPair.objects.get(pk=pair_id)
    except ProjectPair.DoesNotExist:
        return Response({"detail": "not found"}, status=404)
    job = ApplyJob.objects.create(project_pair=pair, mode="generate")
    res = _generate_structure.delay(str(job.id))
    job.celery_task_id = res.id or ""
    job.save(update_fields=["celery_task_id"])
    return Response(ApplyJobSerializer(job).data, status=202)


urlpatterns = urlpatterns + [
    path("project-pairs/<uuid:pair_id>/generate/", trigger_generate, name="trigger-generate"),
]


from .tasks import apply_mappings as _apply_mappings


@api_view(["POST"])
def trigger_apply(request, pair_id):
    try:
        pair = ProjectPair.objects.get(pk=pair_id)
    except ProjectPair.DoesNotExist:
        return Response({"detail": "not found"}, status=404)
    job = ApplyJob.objects.create(project_pair=pair, mode="apply")
    res = _apply_mappings.delay(str(job.id))
    job.celery_task_id = res.id or ""
    job.save(update_fields=["celery_task_id"])
    return Response(ApplyJobSerializer(job).data, status=202)


urlpatterns = urlpatterns + [
    path("project-pairs/<uuid:pair_id>/apply/", trigger_apply, name="trigger-apply"),
]


from .tasks import audit_mappings as _audit_mappings, rotate_master_password as _rotate


@api_view(["POST"])
def trigger_audit(request, pair_id):
    try:
        pair = ProjectPair.objects.get(pk=pair_id)
    except ProjectPair.DoesNotExist:
        return Response({"detail": "not found"}, status=404)
    job = ApplyJob.objects.create(project_pair=pair, mode="audit")
    res = _audit_mappings.delay(str(job.id))
    job.celery_task_id = res.id or ""
    job.save(update_fields=["celery_task_id"])
    return Response(ApplyJobSerializer(job).data, status=202)


@api_view(["POST"])
def trigger_rotation(request):
    old = request.data.get("old_password")
    new = request.data.get("new_password")
    confirm = request.data.get("confirm_new")
    if not old or not new or new != confirm:
        return Response({"detail": "old_password / new_password / confirm_new required and matching"}, status=400)
    result = _rotate.delay(old, new)
    return Response({"task_id": result.id, "status": "dispatched"}, status=202)


urlpatterns = urlpatterns + [
    path("project-pairs/<uuid:pair_id>/audit/", trigger_audit, name="trigger-audit"),
    path("master-key/rotate/", trigger_rotation, name="trigger-rotation"),
]


from .tasks import obfuscate_project as _obfuscate_project


@api_view(["POST"])
def trigger_obfuscate(request, pair_id):
    try:
        pair = ProjectPair.objects.get(pk=pair_id)
    except ProjectPair.DoesNotExist:
        return Response({"detail": "not found"}, status=404)
    seed = request.data.get("seed", "obfusc")
    preserve = request.data.get("preserve_top_package", "ma.ac2i")
    job = ApplyJob.objects.create(project_pair=pair, mode="obfuscate")
    res = _obfuscate_project.delay(str(job.id), seed=seed, preserve_top_package=preserve)
    job.celery_task_id = res.id or ""
    job.save(update_fields=["celery_task_id"])
    return Response(ApplyJobSerializer(job).data, status=202)


urlpatterns = urlpatterns + [
    path("project-pairs/<uuid:pair_id>/obfuscate/", trigger_obfuscate, name="trigger-obfuscate"),
]

