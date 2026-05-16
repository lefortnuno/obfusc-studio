package io.obfuscmapper.parser;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/** XOR + Base64, identical to the Python xor_base64 service. Self-inverse. */
public final class XorBase64 {
    public static String encrypt(String input, String key) {
        byte[] data = input.getBytes(StandardCharsets.UTF_8);
        byte[] k = key.getBytes(StandardCharsets.UTF_8);
        byte[] r = new byte[data.length];
        for (int i = 0; i < data.length; i++) r[i] = (byte)(data[i] ^ k[i % k.length]);
        return Base64.getEncoder().encodeToString(r);
    }
}
