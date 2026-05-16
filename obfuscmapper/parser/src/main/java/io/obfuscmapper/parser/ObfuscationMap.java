package io.obfuscmapper.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The rename map: source fully-qualified name → obfuscated name.
 * Persisted as JSON for traceability and reproducibility.
 */
public class ObfuscationMap {
    public Map<String, String> packages = new LinkedHashMap<>();   // src pkg -> obf pkg
    public Map<String, String> classes = new LinkedHashMap<>();    // src FQCN -> obf FQCN
    public Map<String, String> fields = new LinkedHashMap<>();     // src FQCN.field -> obf field name
    public Map<String, String> methods = new LinkedHashMap<>();    // src FQCN.method -> obf method name
    public Map<String, String> filePathRenames = new LinkedHashMap<>(); // rel src path -> rel tgt path

    public static ObfuscationMap load(Path path) throws IOException {
        ObjectMapper m = new ObjectMapper();
        return m.readValue(path.toFile(), ObfuscationMap.class);
    }

    public void save(Path path) throws IOException {
        ObjectMapper m = new ObjectMapper();
        m.enable(SerializationFeature.INDENT_OUTPUT);
        Files.createDirectories(path.toAbsolutePath().getParent());
        m.writeValue(path.toFile(), this);
    }
}
