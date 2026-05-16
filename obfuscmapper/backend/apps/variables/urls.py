from django.urls import path
from rest_framework.routers import DefaultRouter
from rest_framework.decorators import api_view
from rest_framework.response import Response

from .views import FolderViewSet, SourceFileViewSet, VariableViewSet
from .models import Folder, SourceFile
from apps.projects.models import Project

router = DefaultRouter()
router.register(r"folders", FolderViewSet)
router.register(r"source-files", SourceFileViewSet)
router.register(r"variables", VariableViewSet)


@api_view(["GET"])
def project_tree(request, project_id):
    try:
        project = Project.objects.get(pk=project_id)
    except Project.DoesNotExist:
        return Response({"detail": "not found"}, status=404)

    folders = list(project.folders.all().values("id", "parent_id", "name", "obf_name", "path", "is_default", "validated"))
    files = list(project.files.all().values("id", "folder_id", "name", "obf_name", "relative_path", "language", "is_default", "validated"))

    folder_map = {f["id"]: {**f, "id": str(f["id"]), "parent_id": str(f["parent_id"]) if f["parent_id"] else None, "children": [], "files": []} for f in folders}
    roots = []
    for f in folder_map.values():
        pid = f["parent_id"]
        if pid and pid in folder_map:
            folder_map[pid]["children"].append(f)
        else:
            roots.append(f)
    for sf in files:
        sf2 = {**sf, "id": str(sf["id"]), "folder_id": str(sf["folder_id"]) if sf["folder_id"] else None}
        if sf["folder_id"] and sf["folder_id"] in folder_map:
            folder_map[sf["folder_id"]]["files"].append(sf2)
        else:
            roots.append({**sf2, "_type": "file"})
    return Response({"project_id": str(project.id), "name": project.name, "tree": roots})


urlpatterns = router.urls + [
    path("projects/<uuid:project_id>/tree/", project_tree, name="project-tree"),
]
