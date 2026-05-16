"""Development settings."""
from .base import *  # noqa: F401,F403
import os
from urllib.parse import urlparse

DEBUG = True
ALLOWED_HOSTS = ["*"]

# Force EAGER in dev unless REDIS_URL points to a real Redis
_redis = os.environ.get("REDIS_URL", "")
if not _redis or _redis.startswith("memory"):
    CELERY_TASK_ALWAYS_EAGER = True
    CELERY_BROKER_URL = "memory://"
    CELERY_RESULT_BACKEND = "cache+memory://"
# Allow explicit override via env
if os.environ.get("CELERY_TASK_ALWAYS_EAGER", "").lower() in ("1", "true", "yes"):
    CELERY_TASK_ALWAYS_EAGER = True

db_url = os.environ.get("DATABASE_URL", "")
if db_url.startswith("postgres"):
    parsed = urlparse(db_url)
    DATABASES["default"] = {
        "ENGINE": "django.db.backends.postgresql",
        "NAME": parsed.path.lstrip("/"),
        "USER": parsed.username,
        "PASSWORD": parsed.password,
        "HOST": parsed.hostname,
        "PORT": parsed.port or 5432,
    }
