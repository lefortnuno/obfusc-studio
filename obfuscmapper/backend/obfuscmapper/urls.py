"""Root URL configuration."""
from django.urls import include, path
from drf_spectacular.views import SpectacularAPIView, SpectacularSwaggerView

urlpatterns = [
    path("api/", include("apps.core.urls")),
    path("api/", include("apps.projects.urls")),
    path("api/", include("apps.variables.urls")),
    path("api/", include("apps.mappings.urls")),
    path("api/", include("apps.jobs.urls")),
    path("api/schema/", SpectacularAPIView.as_view(), name="schema"),
    path("api/docs/", SpectacularSwaggerView.as_view(url_name="schema"), name="docs"),
]
