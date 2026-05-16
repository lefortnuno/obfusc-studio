"""XOR + Base64 cipher matching the SecureTransformService.java reference.

The algorithm is its own inverse: encrypt(encrypt(X, key), key) = X.
Encrypt outputs Base64; decrypt accepts Base64 and returns the plaintext.
"""
from __future__ import annotations

import base64


def xor_bytes(data: bytes, key_bytes: bytes) -> bytes:
    if not key_bytes:
        raise ValueError("key must not be empty")
    return bytes(data[i] ^ key_bytes[i % len(key_bytes)] for i in range(len(data)))


def xor_encrypt(plaintext: str, key: str) -> str:
    data = plaintext.encode("utf-8")
    result = xor_bytes(data, key.encode("utf-8"))
    return base64.b64encode(result).decode("ascii")


def xor_decrypt(encoded: str, key: str) -> str:
    data = base64.b64decode(encoded)
    result = xor_bytes(data, key.encode("utf-8"))
    return result.decode("utf-8")


def verify(plaintext: str, encoded: str, key: str) -> bool:
    try:
        return xor_decrypt(encoded, key) == plaintext
    except Exception:
        return False
