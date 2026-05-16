package io.obfuscmapper.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.Callable;

@Command(
    name = "obfusc-parser",
    mixinStandardHelpOptions = true,
    version = "0.2.0",
    description = "Analyse and obfuscate Java projects.",
    subcommands = { ScanCommand.class, ObfuscateCommand.class }
)
public class Main implements Callable<Integer> {

    // Legacy flat-mode options (backwards compat with v0.1 pre-subcommand calls)
    @Option(names = {"-p", "--project"}, description = "Legacy: project root directory.", required = false)
    private Path projectRoot;

    @Option(names = {"-o", "--output"}, description = "Legacy: output JSON file.", required = false)
    private Path output;

    @Option(names = {"--include-private"}, description = "Legacy: include private fields.", defaultValue = "true")
    private boolean includePrivate;

    @Option(names = {"--resolve-strings"}, description = "Legacy: try to evaluate string concat.", defaultValue = "true")
    private boolean resolveStrings;

    @Override
    public Integer call() throws IOException {
        // Legacy mode: behave like the old scan subcommand for backwards compat
        if (projectRoot == null || output == null) {
            new CommandLine(this).usage(System.out);
            return 0;
        }
        return ScanCommand.run(projectRoot, output, includePrivate, resolveStrings);
    }

    public static void main(String[] args) {
        int code = new CommandLine(new Main()).execute(args);
        System.exit(code);
    }
}
