from rest_framework.routers import DefaultRouter
from .views import MappingViewSet

router = DefaultRouter()
router.register(r"mappings", MappingViewSet)
urlpatterns = router.urls
