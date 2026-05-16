package io.obfuscmapper.parser;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

/**
 * Deterministic, opaque-but-valid Java identifier generator.
 * Same input + seed always produce the same output. Collisions handled with suffix.
 */
public final class NamingScheme {

    private static final Set<String> JAVA_RESERVED = Set.of(
        "abstract","assert","boolean","break","byte","case","catch","char","class","const",
        "continue","default","do","double","else","enum","extends","final","finally","float",
        "for","goto","if","implements","import","instanceof","int","interface","long","native",
        "new","null","package","private","protected","public","return","short","static",
        "strictfp","super","switch","synchronized","this","throw","throws","transient","try",
        "void","volatile","while","true","false","var","yield","record","sealed","permits",
        "non-sealed"
    );

    private final String seed;
    private final Set<String> used = new HashSet<>();

    public NamingScheme(String seed) {
        this.seed = seed == null ? "" : seed;
    }

    public String forClass(String fqcn) {
        return claim("O0" + base36Hash(fqcn, 6));
    }

    public String forPackagePart(String pkgPart) {
        String first = pkgPart.isEmpty() ? "p" : String.valueOf(Character.toLowerCase(pkgPart.charAt(0)));
        String h = base36Hash(pkgPart, 3);
        String candidate = first + "0" + h;
        return claim(candidate);
    }

    public String forField(String fqfn) {
        return claim("x0" + base36Hash(fqfn, 6));
    }

    public String forMethod(String fqmn) {
        return claim("m0" + base36Hash(fqmn, 6));
    }

    private String claim(String candidate) {
        String c = sanitize(candidate);
        String pick = c;
        int suf = 0;
        while (used.contains(pick) || JAVA_RESERVED.contains(pick)) {
            suf++;
            pick = c + Integer.toString(suf, 36);
        }
        used.add(pick);
        return pick;
    }

    private String sanitize(String s) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (i == 0) {
                if (!Character.isJavaIdentifierStart(ch)) ch = '_';
            } else {
                if (!Character.isJavaIdentifierPart(ch)) ch = '_';
            }
            b.append(ch);
        }
        return b.toString();
    }

    private String base36Hash(String input, int len) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest((seed + ":" + input).getBytes());
            StringBuilder sb = new StringBuilder();
            long n = 0;
            for (int i = 0; i < 8 && i < hash.length; i++) {
                n = (n << 8) | (hash[i] & 0xff);
            }
            sb.append(Long.toString(Math.abs(n), 36));
            while (sb.length() < len) sb.append('0');
            return sb.substring(0, len);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public void reserve(String name) {
        used.add(name);
    }
}
