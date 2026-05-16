package io.obfuscmapper.parser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class HelperGenerator {

    public static final String CLASS_NAME = "ObfRuntime__$";
    public static final String METHOD_NAME = "$dec$";

    public static String emit(String pkg, String key) {
        StringBuilder b = new StringBuilder();
        String NL = String.valueOf((char)10);
        if (pkg != null && !pkg.isEmpty()) {
            b.append("package ").append(pkg).append(";").append(NL).append(NL);
        }
        b.append("import java.nio.charset.StandardCharsets;").append(NL);
        b.append("import java.util.Base64;").append(NL).append(NL);
        b.append("public final class ").append(CLASS_NAME).append(" {").append(NL);
        b.append("    private static final String K = ").append(stringLit(key)).append(";").append(NL).append(NL);
        b.append("    public static String ").append(METHOD_NAME).append("(String encoded) {").append(NL);
        b.append("        byte[] data = Base64.getDecoder().decode(encoded);").append(NL);
        b.append("        byte[] k = K.getBytes(StandardCharsets.UTF_8);").append(NL);
        b.append("        byte[] r = new byte[data.length];").append(NL);
        b.append("        for (int i = 0; i < data.length; i++) r[i] = (byte)(data[i] ^ k[i % k.length]);").append(NL);
        b.append("        return new String(r, StandardCharsets.UTF_8);").append(NL);
        b.append("    }").append(NL);
        b.append("}").append(NL);
        return b.toString();
    }

    public static void write(Path targetRoot, String pkg, String key) throws IOException {
        String body = emit(pkg, key);
        String relPath = (pkg == null || pkg.isEmpty()) ? CLASS_NAME + ".java"
            : pkg.replace((char)46, (char)47) + "/" + CLASS_NAME + ".java";
        Path out = targetRoot.resolve("src/main/java/" + relPath);
        Files.createDirectories(out.getParent());
        Files.writeString(out, body, StandardCharsets.UTF_8);
    }

    private static String stringLit(String s) {
        StringBuilder sb = new StringBuilder();
        char BS = (char)92;
        char DQ = (char)34;
        sb.append(DQ);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == DQ || c == BS) { sb.append(BS); sb.append(c); }
            else if (c == 10) { sb.append(BS); sb.append((char)110); }
            else if (c == 13) { sb.append(BS); sb.append((char)114); }
            else if (c == 9)  { sb.append(BS); sb.append((char)116); }
            else sb.append(c);
        }
        sb.append(DQ);
        return sb.toString();
    }
}
