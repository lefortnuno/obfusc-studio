"""Base Django settings shared across environments."""
import os
from pathlib import Path

BASE_DIR = Path(__file__).resolve().parent.parent.parent

SECRET_KEY = os.environ.get("DJANGO_SECRET_KEY", "unsafe-default-change-me")
DEBUG = False
ALLOWED_HOSTS = ["localhost", "127.0.0.1"]

INSTALLED_APPS = [
    "django.contrib.contenttypes",
    "django.contrib.auth",
    "django.contrib.staticfiles",
    "rest_framework",
    "corsheaders",
    "drf_spectacular",
    "apps.core",
    "apps.crypto",
    "apps.projects",
    "apps.variables",
    "apps.mappings",
    "apps.jobs",
]

MIDDLEWARE = [
    "corsheaders.middleware.CorsMiddleware",
    "django.middleware.security.SecurityMiddleware",
    "django.middleware.common.CommonMiddleware",
    "django.middleware.csrf.CsrfViewMiddleware",
]

ROOT_URLCONF = "obfuscmapper.urls"

TEMPLATES = [
    {
        "BACKEND": "django.template.backends.django.DjangoTemplates",
        "DIRS": [],
        "APP_DIRS": True,
        "OPTIONS": {},
    },
]

WSGI_APPLICATION = "obfuscmapper.wsgi.application"

DATABASES = {
    "default": {
        "ENGINE": "django.db.backends.sqlite3",
        "NAME": BASE_DIR / "db.sqlite3",
    }
}

DEFAULT_AUTO_FIELD = "django.db.models.BigAutoField"
LANGUAGE_CODE = "fr"
TIME_ZONE = "UTC"
USE_I18N = False
USE_TZ = True
STATIC_URL = "static/"

REST_FRAMEWORK = {
    "DEFAULT_SCHEMA_CLASS": "drf_spectacular.openapi.AutoSchema",
    "DEFAULT_RENDERER_CLASSES": ["rest_framework.renderers.JSONRenderer"],
    "DEFAULT_PARSER_CLASSES": ["rest_framework.parsers.JSONParser"],
}

SPECTACULAR_SETTINGS = {
    "TITLE": "ObfuscMapper API",
    "DESCRIPTION": "API d'orchestration de l'obfuscation source -> cible.",
    "VERSION": "0.1.0",
    "SERVE_INCLUDE_SCHEMA": False,
}

CORS_ALLOWED_ORIGINS = ["http://localhost:5173", "http://127.0.0.1:5173"]
CORS_ALLOW_CREDENTIALS = True

CELERY_BROKER_URL = os.environ.get("REDIS_URL", "memory://")
CELERY_RESULT_BACKEND = os.environ.get("REDIS_URL", "cache+memory://")
CELERY_TASK_ALWAYS_EAGER = False
CELERY_TASK_EAGER_PROPAGATES = True
CELERY_WORKER_CONCURRENCY = 2

LOGGING = {
    "version": 1,
    "disable_existing_loggers": False,
    "formatters": {
        "verbose": {"format": "[{asctime}] {levelname} {name}: {message}", "style": "{"},
    },
    "handlers": {
        "console": {"class": "logging.StreamHandler", "formatter": "verbose"},
    },
    "loggers": {
        "obfuscmapper": {"handlers": ["console"], "level": "INFO"},
    },
}

PARSER_JAR_PATH = os.environ.get(
    "PARSER_JAR_PATH",
    str(BASE_DIR.parent / "parser" / "target" / "obfusc-parser.jar"),
)
import shutil as _shutil
_java_default = (
    "C:/Program Files/Microsoft/jdk-17.0.19.10-hotspot/bin/java.exe"
    if os.path.exists("C:/Program Files/Microsoft/jdk-17.0.19.10-hotspot/bin/java.exe")
    else (_shutil.which("java") or "java")
)
JAVA_BIN = os.environ.get("JAVA_BIN", _java_default)
