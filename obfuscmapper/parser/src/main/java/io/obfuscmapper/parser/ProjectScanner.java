package io.obfuscmapper.parser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.LiteralStringValueExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.TextBlockLiteralExpr;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class ProjectScanner {
    private static final String BACKSLASH = String.valueOf((char) 92);
    private static final String SLASH = "/";

    private final Path root;
    private final boolean includePrivate;
    private final boolean resolveStrings;

    public ProjectScanner(Path root, boolean includePrivate, boolean resolveStrings) {
        this.root = root;
        this.includePrivate = includePrivate;
        this.resolveStrings = resolveStrings;
    }

    private static String normalize(String s) { return s.replace(BACKSLASH, SLASH); }

    public Map<String, Object> scan() throws IOException {
        List<Map<String, Object>> folders = new ArrayList<>();
        List<Map<String, Object>> files = new ArrayList<>();
        List<Map<String, Object>> variables = new ArrayList<>();
        Set<String> seenFolders = new HashSet<>();

        try (Stream<Path> stream = Files.walk(root)) {
            stream.filter(p -> p.toString().endsWith(".java"))
                  .forEach(jf -> processJavaFile(jf, files, variables, folders, seenFolders));
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("project_root", root.toString());
        result.put("folders", folders);
        result.put("files", files);
        result.put("variables", variables);
        return result;
    }

    private void processJavaFile(Path javaFile, List<Map<String, Object>> files,
                                  List<Map<String, Object>> variables,
                                  List<Map<String, Object>> folders,
                                  Set<String> seenFolders) {
        try {
            Path rel = root.relativize(javaFile);
            String relStr = normalize(rel.toString());
            recordFolders(rel, folders, seenFolders);

            CompilationUnit cu = StaticJavaParser.parse(javaFile);
            String packageName = cu.getPackageDeclaration().map(pd -> pd.getNameAsString()).orElse("");
            String className = cu.getPrimaryTypeName().orElse(javaFile.getFileName().toString().replace(".java", ""));

            Map<String, Object> fileEntry = new LinkedHashMap<>();
            fileEntry.put("path", relStr);
            fileEntry.put("name", javaFile.getFileName().toString());
            fileEntry.put("class_name", className);
            fileEntry.put("package", packageName);
            files.add(fileEntry);

            collectFields(cu, relStr, variables);
        } catch (Exception e) {
            System.err.println("Skipping " + javaFile + ": " + e.getMessage());
        }
    }

    private void recordFolders(Path rel, List<Map<String, Object>> folders, Set<String> seenFolders) {
        Path parentDir = rel.getParent();
        while (parentDir != null) {
            String parentStr = normalize(parentDir.toString());
            if (!seenFolders.contains(parentStr)) {
                seenFolders.add(parentStr);
                Map<String, Object> folder = new LinkedHashMap<>();
                folder.put("path", parentStr);
                folder.put("name", parentDir.getFileName().toString());
                Path gp = parentDir.getParent();
                folder.put("parent", gp == null ? null : normalize(gp.toString()));
                folders.add(folder);
            }
            parentDir = parentDir.getParent();
        }
    }

    private void collectFields(CompilationUnit cu, String relStr, List<Map<String, Object>> variables) {
        for (FieldDeclaration field : cu.findAll(FieldDeclaration.class)) {
            if (!includePrivate && field.isPrivate()) continue;
            for (VariableDeclarator var : field.getVariables()) {
                if (!isString(var.getTypeAsString())) continue;
                Map<String, Object> varEntry = new LinkedHashMap<>();
                varEntry.put("file_path", relStr);
                varEntry.put("name", var.getNameAsString());
                varEntry.put("type", var.getTypeAsString());
                varEntry.put("access", field.getAccessSpecifier().asString());
                varEntry.put("location", "field");
                if (field.getRange().isPresent()) {
                    varEntry.put("line_start", field.getRange().get().begin.line);
                    varEntry.put("line_end", field.getRange().get().end.line);
                }
                Optional<Expression> init = var.getInitializer();
                if (init.isPresent()) {
                    ResolutionResult rr = resolveExpression(init.get());
                    varEntry.put("raw_value", init.get().toString());
                    varEntry.put("evaluated_value", rr.value);
                    varEntry.put("confidence", rr.confidence);
                    if (rr.reason != null) varEntry.put("reason", rr.reason);
                } else {
                    ResolutionResult rr = findAssignment(cu, var.getNameAsString());
                    if (rr != null) {
                        varEntry.put("raw_value", rr.raw);
                        varEntry.put("evaluated_value", rr.value);
                        varEntry.put("confidence", rr.confidence);
                        varEntry.put("location", "constructor");
                        if (rr.reason != null) varEntry.put("reason", rr.reason);
                    } else {
                        varEntry.put("confidence", "low");
                        varEntry.put("reason", "no initializer or assignment found");
                    }
                }
                variables.add(varEntry);
            }
        }
    }

    private boolean isString(String t) {
        return "String".equals(t) || t.endsWith(".String") || "java.lang.String".equals(t);
    }

    private ResolutionResult resolveExpression(Expression expr) {
        if (!resolveStrings) {
            ResolutionResult r = new ResolutionResult();
            r.confidence = "low";
            r.reason = "string resolution disabled";
            return r;
        }
        if (expr instanceof StringLiteralExpr) {
            ResolutionResult r = new ResolutionResult();
            r.value = ((StringLiteralExpr) expr).getValue();
            r.confidence = "high";
            return r;
        }
        if (expr instanceof TextBlockLiteralExpr) {
            ResolutionResult r = new ResolutionResult();
            r.value = ((TextBlockLiteralExpr) expr).asString();
            r.confidence = "high";
            return r;
        }
        if (expr instanceof BinaryExpr) {
            BinaryExpr be = (BinaryExpr) expr;
            if (be.getOperator() == BinaryExpr.Operator.PLUS) {
                ResolutionResult left = resolveExpression(be.getLeft());
                ResolutionResult right = resolveExpression(be.getRight());
                ResolutionResult r = new ResolutionResult();
                if ("low".equals(left.confidence) || "low".equals(right.confidence) || left.value == null || right.value == null) {
                    r.confidence = "low";
                    r.reason = "concat with non-literal part";
                    return r;
                }
                r.value = left.value + right.value;
                r.confidence = "high";
                return r;
            }
        }
        if (expr instanceof LiteralStringValueExpr) {
            ResolutionResult r = new ResolutionResult();
            r.value = ((LiteralStringValueExpr) expr).getValue();
            r.confidence = "high";
            return r;
        }
        ResolutionResult r = new ResolutionResult();
        r.confidence = "low";
        r.reason = "unsupported expression: " + expr.getClass().getSimpleName();
        return r;
    }

    private ResolutionResult findAssignment(CompilationUnit cu, String fieldName) {
        for (AssignExpr a : cu.findAll(AssignExpr.class)) {
            String target = a.getTarget().toString();
            if (target.equals("this." + fieldName) || target.equals(fieldName)) {
                ResolutionResult r = resolveExpression(a.getValue());
                r.raw = a.getValue().toString();
                return r;
            }
        }
        return null;
    }

    private static class ResolutionResult {
        String raw;
        String value;
        String confidence;
        String reason;
    }
}
