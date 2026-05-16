"""Fernet-based encryption of sensitive DB fields, derived from a master password."""
from __future__ import annotations

import base64
import os

from cryptography.fernet import Fernet, InvalidToken
from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.kdf.pbkdf2 import PBKDF2HMAC


PBKDF2_ITERATIONS = 600_000
SALT_LENGTH = 16


def derive_key(master_password: str, salt: bytes) -> bytes:
    """PBKDF2-SHA256 derivation, returning a 32-byte url-safe Fernet key."""
    if not master_password:
        raise ValueError("master_password must not be empty")
    if len(salt) != SALT_LENGTH:
        raise ValueError("salt must be %d bytes" % SALT_LENGTH)
    kdf = PBKDF2HMAC(
        algorithm=hashes.SHA256(),
        length=32,
        salt=salt,
        iterations=PBKDF2_ITERATIONS,
    )
    raw = kdf.derive(master_password.encode("utf-8"))
    return base64.urlsafe_b64encode(raw)


def make_fernet(master_password: str, salt: bytes) -> Fernet:
    return Fernet(derive_key(master_password, salt))


def encrypt_value(plaintext: str, fernet: Fernet) -> str:
    return fernet.encrypt(plaintext.encode("utf-8")).decode("ascii")


def decrypt_value(token: str, fernet: Fernet) -> str:
    return fernet.decrypt(token.encode("ascii")).decode("utf-8")


def new_salt() -> bytes:
    return os.urandom(SALT_LENGTH)


__all__ = [
    "derive_key",
    "make_fernet",
    "encrypt_value",
    "decrypt_value",
    "new_salt",
    "InvalidToken",
    "PBKDF2_ITERATIONS",
    "SALT_LENGTH",
]
