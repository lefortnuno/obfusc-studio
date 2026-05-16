"""Production settings."""
from .base import *  # noqa: F401,F403
import os
from urllib.parse import urlparse

DEBUG = False
ALLOWED_HOSTS = os.environ.get("DJANGO_ALLOWED_HOSTS", "").split(",")

db_url = os.environ["DATABASE_URL"]
parsed = urlparse(db_url)
DATABASES["default"] = {
    "ENGINE": "django.db.backends.postgresql",
    "NAME": parsed.path.lstrip("/"),
    "USER": parsed.username,
    "PASSWORD": parsed.password,
    "HOST": parsed.hostname,
    "PORT": parsed.port or 5432,
}
