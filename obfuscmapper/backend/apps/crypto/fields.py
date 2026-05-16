"""EncryptedTextField: transparently Fernet-encrypts text columns."""
from __future__ import annotations

from django.db import models

from .services.master_key import get_fernet
from .services.fernet import encrypt_value, decrypt_value


class EncryptedTextField(models.TextField):
    """A TextField that encrypts values at write time and decrypts at read time.

    The master key must be initialized in the current process before any
    save() or load() operation occurs.
    """
    description = "Fernet-encrypted text"

    def from_db_value(self, value, expression, connection):
        if value is None or value == "":
            return value
        try:
            return decrypt_value(value, get_fernet())
        except Exception:
            return value

    def to_python(self, value):
        if value is None or value == "":
            return value
        return value

    def get_prep_value(self, value):
        if value is None or value == "":
            return value
        if isinstance(value, bytes):
            value = value.decode("utf-8")
        return encrypt_value(value, get_fernet())
