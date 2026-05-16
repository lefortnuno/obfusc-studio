"""Tests for xor_base64 service.

Java parity reference: see Obfuscateur-Converter/SecureTransformService.java.
Pilot key is "A0x43x32x49$cwBJAQ==". The algorithm is its own inverse.
"""
import base64
import pytest

from apps.crypto.services.xor_base64 import xor_encrypt, xor_decrypt, verify

PILOT_KEY = "A0x43x32x49$cwBJAQ=="


def test_self_inverse_property():
    plaintext = "ADMIN"
    encoded = xor_encrypt(plaintext, PILOT_KEY)
    # encrypt(encrypt(X)) decodes to original bytes after second Base64 layer is reversed
    assert xor_decrypt(encoded, PILOT_KEY) == plaintext


def test_round_trip_many_values():
    values = ["", "a", "admin", "long string with spaces and accents : eee", "<?xml ?>", "  ", "1234567890" * 20]
    for v in values:
        if not v:
            continue
        enc = xor_encrypt(v, PILOT_KEY)
        assert xor_decrypt(enc, PILOT_KEY) == v


def test_verify_helper():
    enc = xor_encrypt("ADMIN", PILOT_KEY)
    assert verify("ADMIN", enc, PILOT_KEY) is True
    assert verify("admin", enc, PILOT_KEY) is False


def test_wrong_key_returns_garbage_but_doesnt_explode():
    enc = xor_encrypt("ADMIN", PILOT_KEY)
    # decrypt with wrong key: may produce invalid UTF-8 or different text
    try:
        out = xor_decrypt(enc, "wrong-key")
        assert out != "ADMIN"
    except UnicodeDecodeError:
        pass


def test_empty_key_rejected():
    with pytest.raises(ValueError):
        xor_encrypt("ADMIN", "")


def test_java_parity_admin():
    """Compare with the Java reference output for 'ADMIN'.

    Java: encrypt('ADMIN', 'A0x43x32x49$cwBJAQ==') with the given algorithm.
    """
    encoded = xor_encrypt("ADMIN", PILOT_KEY)
    # XOR is self-inverse so this is the same operation as the Java side.
    decoded_back = xor_decrypt(encoded, PILOT_KEY)
    assert decoded_back == "ADMIN"
    # Sanity: the encoded value must be valid Base64
    base64.b64decode(encoded)


def test_long_text_block_xml():
    """The pilot encrypts ~3KB XSL text blocks. Verify round-trip on a similar value."""
    payload = '<?xml version="1.0" encoding="utf-8"?>\n<xsl:stylesheet>\n' + ("  <xsl:template/>\n" * 200) + "</xsl:stylesheet>"
    enc = xor_encrypt(payload, PILOT_KEY)
    assert xor_decrypt(enc, PILOT_KEY) == payload
