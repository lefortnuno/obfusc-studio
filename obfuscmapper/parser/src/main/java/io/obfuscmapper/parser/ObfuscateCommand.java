package io.obfuscmapper.parser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.printer.DefaultPrettyPrinter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

@Command(name = "obfuscate", description = "Apply full source-to-target obfuscation.")
public class ObfuscateCommand implements Callable<Integer> {

    @Option(names = {"-s", "--source"}, description = "Source project root.", required = true)
    private Path source;

    @Option(names = {"-t", "--target"}, description = "Target project root (will be created/overwritten).", required = true)
    private Path target;

    @Option(names = {"-k", "--key"}, description = "XOR encryption key.", required = true)
    private String key;

    @Option(names = {"--seed"}, description = "Hash seed for deterministic naming.", defaultValue = "obfusc")
    private String seed;

    @Option(names = {"--preserve-top-package"}, description = "Top package prefix to keep unchanged.", defaultValue = "")
    private String preservedTopPkg;

    @Option(names = {"--map-out"}, description = "Where to write the generated obfuscation map JSON.")
    private Path mapOut;

    @Option(names = {"--encrypt-strings"}, description = "Encrypt string literals.", defaultValue = "true")
    private boolean encryptStrings;

    @Option(names = {"--min-string-len"}, description = "Minimum string length to encrypt.", defaultValue = "3")
    private int minStringLen;

    @Option(names = {"--helper-package"}, description = "Package for the R04oo decryption helper.", defaultValue = "s0o")
    private String helperPackage;

    @Override
    public Integer call() throws IOException {
        ParserConfig.init();
        if (!Files.isDirectory(source)) {
            System.err.println("Source not found: " + source);
            return 2;
        }
        Files.createDirectories(target);

        System.out.println("[1/5] Collecting symbols from " + source);
        SymbolCollector.Collected col = new SymbolCollector(source).collect();
        System.out.println("  classes=" + col.classes.size() + " fields=" + col.fields.size() + " methods=" + col.methods.size() + " packages=" + col.packages.size());
        System.out.println("  preserve_classes=" + col.preserveClasses.size() + " preserve_methods=" + col.preserveMethods.size());

        System.out.println("[2/5] Building obfuscation map (seed=" + seed + ")");
        ObfuscationMap map = MapBuilder.build(col, seed, preservedTopPkg);

        // Compute helper FQCN
        String helperFqcn = helperPackage.isEmpty() ? HelperGenerator.CLASS_NAME : helperPackage + "." + HelperGenerator.CLASS_NAME;
        // Reserve it in the map so it is not re-used
        map.classes.put(helperFqcn, helperFqcn);
        // Also reserve the helper method name (do not rename calls to it)
        map.methods.put(helperFqcn + "." + HelperGenerator.METHOD_NAME, HelperGenerator.METHOD_NAME);

        if (mapOut != null) {
            map.save(mapOut);
            System.out.println("  map written to " + mapOut);
        }

        System.out.println("[3/5] Writing helper " + helperFqcn);
        HelperGenerator.write(target, helperPackage, key);

        System.out.println("[4/5] Transforming Java source files");
        SourceTransformer transformer = new SourceTransformer(map, key, helperFqcn, encryptStrings, minStringLen);
        int processed = 0;
        int errors = 0;
        List<Path> javaFiles = new ArrayList<>();
        try (Stream<Path> s = Files.walk(source)) {
            s.filter(p -> p.toString().endsWith(".java")).forEach(javaFiles::add);
        }
        for (Path jf : javaFiles) {
            try {
                CompilationUnit cu = StaticJavaParser.parse(jf);
                transformer.transform(cu);
                Path relSrc = source.relativize(jf);
                String relStr = relSrc.toString().replace((char)92, '/');
                String relTgtStr = map.filePathRenames.getOrDefault(relStr, relStr);
                Path outFile = target.resolve(relTgtStr);
                Files.createDirectories(outFile.getParent());
                String body = new DefaultPrettyPrinter().print(cu);
                Files.writeString(outFile, body, StandardCharsets.UTF_8);
                processed++;
            } catch (Exception e) {
                System.err.println("Error transforming " + jf + ": " + e.getMessage());
                errors++;
            }
        }
        System.out.println("  java files processed=" + processed + " errors=" + errors);

        System.out.println("[5/5] Copying non-Java resources");
        int copied = 0;
        try (Stream<Path> s = Files.walk(source)) {
            for (Path p : (Iterable<Path>) s::iterator) {
                if (Files.isDirectory(p)) continue;
                String n = p.getFileName().toString();
                if (n.endsWith(".java")) continue;
                if (p.toString().contains(java.io.File.separator + "target" + java.io.File.separator) || p.toString().contains("/target/")) continue;
                if (p.toString().contains(java.io.File.separator + ".git" + java.io.File.separator) || p.toString().contains("/.git/")) continue;
                Path rel = source.relativize(p);
                Path out = target.resolve(rel);
                Files.createDirectories(out.getParent());
                // Rewrite pom.xml main-class if present
                if (n.equals("pom.xml")) {
                    String body = Files.readString(p, StandardCharsets.UTF_8);
                    for (Map.Entry<String, String> e : map.classes.entrySet()) {
                        body = body.replace(e.getKey(), e.getValue());
                    }
                    Files.writeString(out, body, StandardCharsets.UTF_8);
                } else {
                    Files.copy(p, out, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
                copied++;
            }
        }
        System.out.println("  resources copied=" + copied);
        System.out.println("DONE. Obfuscated project ready at " + target);
        return errors == 0 ? 0 : 1;
    }
}
