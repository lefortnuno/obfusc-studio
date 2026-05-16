package io.obfuscmapper.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.Callable;

@Command(name = "scan", description = "Scan a Java project and emit a JSON of variables.")
public class ScanCommand implements Callable<Integer> {

    @Option(names = {"-p", "--project"}, description = "Project root directory.", required = true)
    private Path projectRoot;

    @Option(names = {"-o", "--output"}, description = "Output JSON file.", required = true)
    private Path output;

    @Option(names = {"--include-private"}, description = "Include private fields.", defaultValue = "true")
    private boolean includePrivate;

    @Option(names = {"--resolve-strings"}, description = "Try to evaluate string concat/text blocks.", defaultValue = "true")
    private boolean resolveStrings;

    @Override
    public Integer call() throws IOException {
        return run(projectRoot, output, includePrivate, resolveStrings);
    }

    public static Integer run(Path projectRoot, Path output, boolean includePrivate, boolean resolveStrings) throws IOException {
        ParserConfig.init();
        if (!Files.isDirectory(projectRoot)) {
            System.err.println("Project root not found: " + projectRoot);
            return 2;
        }
        ProjectScanner scanner = new ProjectScanner(projectRoot, includePrivate, resolveStrings);
        Map<String, Object> result = scanner.scan();

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        Files.createDirectories(output.toAbsolutePath().getParent());
        mapper.writeValue(output.toFile(), result);
        System.out.println("Wrote " + output);
        return 0;
    }
}
