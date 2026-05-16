"""Tests for the Fernet service."""
import pytest

from cryptography.fernet import InvalidToken

from apps.crypto.services.fernet import (
    derive_key,
    make_fernet,
    encrypt_value,
    decrypt_value,
    new_salt,
    SALT_LENGTH,
)


def test_derive_key_deterministic():
    salt = b"\x00" * SALT_LENGTH
    k1 = derive_key("pass-1234", salt)
    k2 = derive_key("pass-1234", salt)
    assert k1 == k2
    assert len(k1) == 44  # base64 of 32 bytes = 44 chars


def test_derive_key_different_password():
    salt = b"\x00" * SALT_LENGTH
    assert derive_key("p1", salt) != derive_key("p2", salt)


def test_derive_key_different_salt():
    p = "same-password"
    assert derive_key(p, b"\x00" * SALT_LENGTH) != derive_key(p, b"\x01" * SALT_LENGTH)


def test_round_trip():
    salt = new_salt()
    f = make_fernet("master-password", salt)
    cipher = encrypt_value("hello", f)
    assert decrypt_value(cipher, f) == "hello"


def test_wrong_password_raises():
    salt = new_salt()
    f1 = make_fernet("right", salt)
    f2 = make_fernet("wrong", salt)
    cipher = encrypt_value("payload", f1)
    with pytest.raises(InvalidToken):
        decrypt_value(cipher, f2)


def test_empty_password_rejected():
    with pytest.raises(ValueError):
        derive_key("", b"\x00" * SALT_LENGTH)


def test_bad_salt_length_rejected():
    with pytest.raises(ValueError):
        derive_key("ok", b"short")
