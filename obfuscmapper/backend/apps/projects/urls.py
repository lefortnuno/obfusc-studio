from rest_framework.routers import DefaultRouter
from .views import ProjectViewSet, ProjectPairViewSet

router = DefaultRouter()
router.register(r"projects", ProjectViewSet)
router.register(r"project-pairs", ProjectPairViewSet)

urlpatterns = router.urls
