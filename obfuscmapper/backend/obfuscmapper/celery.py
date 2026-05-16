"""Celery application factory."""
import os
from celery import Celery

os.environ.setdefault("DJANGO_SETTINGS_MODULE", "obfuscmapper.settings.development")

app = Celery("obfuscmapper")
app.config_from_object("django.conf:settings", namespace="CELERY")
app.autodiscover_tasks()
