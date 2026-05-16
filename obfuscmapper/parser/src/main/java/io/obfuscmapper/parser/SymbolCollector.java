package io.obfuscmapper.parser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

/**
 * Walks all .java files and collects every renameable symbol (package, class, field, method),
 * skipping Spring-essential identifiers (annotated controllers, autowired beans).
 */
public class SymbolCollector {

    private static final Set<String> SPRING_PRESERVE_ANNOTATIONS = Set.of(
        "Controller", "RestController", "Service", "Repository", "Component", "Configuration",
        "Entity", "MappedSuperclass", "Embeddable", "EnableWebSecurity", "EnableWebMvc",
        "SpringBootApplication"
    );

    /** Classes annotated with these should have their FIELDS preserved too (JPA columns, Lombok getters). */
    private static final Set<String> FIELD_PRESERVE_ANNOTATIONS = Set.of(
        "Entity", "MappedSuperclass", "Embeddable", "Data", "Getter", "Setter", "RequiredArgsConstructor",
        "AllArgsConstructor", "NoArgsConstructor", "Builder", "Value",
        "Controller", "RestController", "Service", "Repository", "Component", "Configuration",
        "ControllerAdvice", "RestControllerAdvice"
    );

    public static class Collected {
        public final Set<String> packages = new LinkedHashSet<>();
        public final Set<String> classes = new LinkedHashSet<>();
        // FQCN.field
        public final Set<String> fields = new LinkedHashSet<>();
        // FQCN.method (without param signature)
        public final Set<String> methods = new LinkedHashSet<>();
        // Classes that MUST keep their original name (Spring annotated, main class, etc.)
        public final Set<String> preserveClasses = new LinkedHashSet<>();
        // Method names that MUST stay (main, hashCode, equals, toString, public Spring callbacks)
        public final Set<String> preserveMethods = new LinkedHashSet<>();
        // Source file relative paths -> FQCN of primary type
        public final Map<String, String> fileToPrimary = new LinkedHashMap<>();
    }

    private final Path root;

    public SymbolCollector(Path root) {
        this.root = root;
    }

    public Collected collect() throws IOException {
        Collected c = new Collected();
        try (Stream<Path> stream = Files.walk(root)) {
            stream.filter(p -> p.toString().endsWith(".java"))
                  .sorted()
                  .forEach(p -> processFile(p, c));
        }
        return c;
    }

    private void processFile(Path javaFile, Collected c) {
        try {
            CompilationUnit cu = StaticJavaParser.parse(javaFile);
            String pkg = cu.getPackageDeclaration().map(pd -> pd.getNameAsString()).orElse("");
            if (!pkg.isEmpty()) c.packages.add(pkg);

            Path rel = root.relativize(javaFile);
            String relStr = rel.toString().replace(java.io.File.separatorChar, '/');

            String firstClassFqn = null;
            for (TypeDeclaration<?> td : cu.getTypes()) {
                String simple = td.getNameAsString();
                String fqcn = pkg.isEmpty() ? simple : pkg + "." + simple;
                c.classes.add(fqcn);
                if (firstClassFqn == null) firstClassFqn = fqcn;

                boolean preserve = false;
                for (AnnotationExpr a : td.getAnnotations()) {
                    if (SPRING_PRESERVE_ANNOTATIONS.contains(a.getNameAsString())) {
                        preserve = true; break;
                    }
                }
                if (preserve) c.preserveClasses.add(fqcn);

                if (td instanceof ClassOrInterfaceDeclaration) {
                    ClassOrInterfaceDeclaration cd = (ClassOrInterfaceDeclaration) td;
                    boolean preserveFields = false;
                    boolean isInterface = cd.isInterface();
                    if (isInterface) {
                        // Interfaces are likely Spring Data repos or callback contracts; preserve everything
                        c.preserveClasses.add(fqcn);
                    }
                    for (AnnotationExpr a : td.getAnnotations()) {
                        if (FIELD_PRESERVE_ANNOTATIONS.contains(a.getNameAsString())) {
                            preserveFields = true; break;
                        }
                    }
                    for (FieldDeclaration f : cd.getFields()) {
                        for (VariableDeclarator v : f.getVariables()) {
                            if (!preserveFields) {
                                c.fields.add(fqcn + "." + v.getNameAsString());
                            }
                        }
                    }
                    for (MethodDeclaration m : cd.getMethods()) {
                        String mName = m.getNameAsString();
                        if (isInterface) {
                            c.preserveMethods.add(fqcn + "." + mName);
                            c.methods.add(fqcn + "." + mName);
                            continue;
                        }
                        // Preserve common method names
                        if (mName.equals("main") || mName.equals("toString") || mName.equals("equals") || mName.equals("hashCode")) {
                            c.preserveMethods.add(fqcn + "." + mName);
                            continue;
                        }
                        // Preserve public getters/setters used by Spring bean introspection
                        if (m.isPublic() && (mName.startsWith("get") || mName.startsWith("set") || mName.startsWith("is"))) {
                            c.preserveMethods.add(fqcn + "." + mName);
                            continue;
                        }
                        // Preserve methods with Spring annotations (RequestMapping, etc.)
                        boolean keep = false;
                        for (AnnotationExpr a : m.getAnnotations()) {
                            String an = a.getNameAsString();
                            if (an.endsWith("Mapping") || an.equals("Bean") || an.equals("PostConstruct") || an.equals("PreDestroy")
                                || an.equals("EventListener") || an.equals("Scheduled") || an.equals("Override")) {
                                keep = true; break;
                            }
                        }
                        if (keep) c.preserveMethods.add(fqcn + "." + mName);
                        c.methods.add(fqcn + "." + mName);
                    }
                }
            }
            if (firstClassFqn != null) c.fileToPrimary.put(relStr, firstClassFqn);
        } catch (Exception e) {
            System.err.println("SymbolCollector skipped " + javaFile + ": " + e.getMessage());
        }
    }
}
