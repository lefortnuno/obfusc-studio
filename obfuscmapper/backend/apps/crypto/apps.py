import logging
from django.apps import AppConfig

logger = logging.getLogger(__name__)


class CryptoConfig(AppConfig):
    default_auto_field = "django.db.models.BigAutoField"
    name = "apps.crypto"

    def ready(self):
        from .services.master_key import init_from_env
        if init_from_env():
            logger.info("Master key initialized from environment.")
        else:
            logger.warning(
                "MASTER_PASSWORD env var not set; encrypted fields will fail until set via API."
            )
