"""Process-local master key context.

The master password is never persisted. The Fernet key (and the salt that
produced it) live in memory only for the lifetime of the worker process.
"""
from __future__ import annotations

import logging
import os
import threading
from typing import Optional

from cryptography.fernet import Fernet

from .fernet import derive_key, new_salt

logger = logging.getLogger(__name__)


class _MasterKeyContext:
    def __init__(self) -> None:
        self._lock = threading.Lock()
        self._salt: Optional[bytes] = None
        self._fernet: Optional[Fernet] = None

    def set_password(self, master_password: str, salt: Optional[bytes] = None) -> None:
        with self._lock:
            if salt is None:
                salt = new_salt()
            self._salt = salt
            self._fernet = Fernet(derive_key(master_password, salt))

    def get(self) -> Fernet:
        with self._lock:
            if self._fernet is None:
                raise RuntimeError(
                    "Master key context not initialized. Call set_password() first."
                )
            return self._fernet

    def get_salt(self) -> bytes:
        with self._lock:
            if self._salt is None:
                raise RuntimeError("Salt not set.")
            return self._salt

    def clear(self) -> None:
        with self._lock:
            self._salt = None
            self._fernet = None


_context = _MasterKeyContext()


def set_master_password(password: str, salt: Optional[bytes] = None) -> None:
    _context.set_password(password, salt)


def get_fernet() -> Fernet:
    return _context.get()


def get_salt() -> bytes:
    return _context.get_salt()


def clear_master_password() -> None:
    _context.clear()


def init_from_env() -> bool:
    """Initialize the context from MASTER_PASSWORD env var. Returns True on success."""
    pwd = os.environ.get("MASTER_PASSWORD")
    if not pwd:
        return False
    salt_b64 = os.environ.get("MASTER_SALT")
    salt = None
    if salt_b64:
        import base64
        salt = base64.urlsafe_b64decode(salt_b64)
    set_master_password(pwd, salt=salt)
    return True
