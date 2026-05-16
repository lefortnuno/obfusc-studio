package io.obfuscmapper.parser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.TextBlockLiteralExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import java.util.Map;

public class SourceTransformer {

    private final ObfuscationMap map;
    private final String key;
    private final String helperFqcn;
    private final boolean encryptStrings;
    private final int minStringLen;
    private final java.util.Map<String, String> simpleMethodMap = new java.util.HashMap<>();
    private final java.util.Set<String> ambiguousMethods = new java.util.HashSet<>();
    private final java.util.Map<String, String> simpleFieldMap = new java.util.HashMap<>();
    private final java.util.Set<String> ambiguousFields = new java.util.HashSet<>();

    public SourceTransformer(ObfuscationMap map, String key, String helperFqcn, boolean encryptStrings, int minStringLen) {
        this.map = map;
        this.key = key;
        this.helperFqcn = helperFqcn;
        this.encryptStrings = encryptStrings;
        this.minStringLen = minStringLen;
        // Build simple-name maps; if two source methods/fields rename to different targets, mark ambiguous (skip rename)
        for (java.util.Map.Entry<String, String> e : map.methods.entrySet()) {
            String simple = e.getKey().substring(e.getKey().lastIndexOf('.') + 1);
            String newName = e.getValue();
            if (simple.equals(newName)) continue;
            if (simpleMethodMap.containsKey(simple) && !simpleMethodMap.get(simple).equals(newName)) {
                ambiguousMethods.add(simple);
            } else {
                simpleMethodMap.put(simple, newName);
            }
        }
        for (java.util.Map.Entry<String, String> e : map.fields.entrySet()) {
            String simple = e.getKey().substring(e.getKey().lastIndexOf('.') + 1);
            String newName = e.getValue();
            if (simple.equals(newName)) continue;
            if (simpleFieldMap.containsKey(simple) && !simpleFieldMap.get(simple).equals(newName)) {
                ambiguousFields.add(simple);
            } else {
                simpleFieldMap.put(simple, newName);
            }
        }
    }

    public CompilationUnit transform(CompilationUnit cu) {
        String srcPkg = cu.getPackageDeclaration().map(p -> p.getNameAsString()).orElse("");
        String dstPkg = map.packages.getOrDefault(srcPkg, srcPkg);
        if (!srcPkg.equals(dstPkg)) {
            cu.setPackageDeclaration(new PackageDeclaration(new Name(dstPkg)));
        }

        for (ImportDeclaration imp : cu.getImports()) {
            String name = imp.getNameAsString();
            if (imp.isAsterisk()) {
                // Wildcard import: name is a package, rename if mapped
                String pkgRenamed = map.packages.get(name);
                if (pkgRenamed != null) {
                    imp.setName(pkgRenamed);
                }
                continue;
            }
            String renamed = map.classes.get(name);
            if (renamed != null) {
                imp.setName(renamed);
            } else {
                int dot = name.lastIndexOf('.');
                if (dot > 0) {
                    String pkgOnly = name.substring(0, dot);
                    String pkgRenamed = map.packages.get(pkgOnly);
                    if (pkgRenamed != null) {
                        imp.setName(pkgRenamed + "." + name.substring(dot + 1));
                    }
                }
            }
        }

        if (encryptStrings && !helperFqcn.isEmpty()) {
            boolean already = cu.getImports().stream().anyMatch(i -> i.getNameAsString().equals(helperFqcn));
            String currentPkg = cu.getPackageDeclaration().map(p -> p.getNameAsString()).orElse("");
            String helperPkg = helperFqcn.contains(".") ? helperFqcn.substring(0, helperFqcn.lastIndexOf('.')) : "";
            if (!already && !helperPkg.equals(currentPkg)) {
                cu.addImport(helperFqcn);
            }
        }

        for (TypeDeclaration<?> td : cu.getTypes()) {
            renameTypeDecl(td, srcPkg);
        }

        renameReferences(cu);

        // Cross-file method calls
        // Build reverse map: renamed simple class name -> list of original FQCNs (for static calls like Foo.bar())
        java.util.Map<String, java.util.List<String>> renamedClassToOriginals = new java.util.HashMap<>();
        for (Map.Entry<String, String> e : map.classes.entrySet()) {
            String renamedSimple = MapBuilder.simpleName(e.getValue());
            renamedClassToOriginals.computeIfAbsent(renamedSimple, k -> new java.util.ArrayList<>()).add(e.getKey());
        }
        cu.findAll(com.github.javaparser.ast.expr.MethodCallExpr.class).forEach(mc -> {
            String mn = mc.getNameAsString();
            // First try scoped resolution if scope is a simple class name
            if (mc.getScope().isPresent() && mc.getScope().get() instanceof com.github.javaparser.ast.expr.NameExpr) {
                String scope = ((com.github.javaparser.ast.expr.NameExpr) mc.getScope().get()).getNameAsString();
                java.util.List<String> originals = renamedClassToOriginals.get(scope);
                if (originals != null) {
                    for (String origFqcn : originals) {
                        String origMethodKey = origFqcn + "." + mn;
                        // Also check by reverse-rename: method might still have original name in source
                        String renamed = map.methods.get(origMethodKey);
                        if (renamed != null && !renamed.equals(mn)) {
                            mc.setName(renamed);
                            return;
                        }
                    }
                }
            }
            if (ambiguousMethods.contains(mn)) return;
            String renamed = simpleMethodMap.get(mn);
            if (renamed != null && !renamed.equals(mn)) {
                mc.setName(renamed);
            }
        });
        // Cross-file field accesses: rename xxx.someField when someField was renamed source-wide
        cu.findAll(com.github.javaparser.ast.expr.FieldAccessExpr.class).forEach(fa -> {
            String fn = fa.getNameAsString();
            if (ambiguousFields.contains(fn)) return;
            String renamed = simpleFieldMap.get(fn);
            if (renamed != null && !renamed.equals(fn)) {
                fa.setName(renamed);
            }
        });

        if (encryptStrings) {
            encryptLiterals(cu);
        }

        return cu;
    }

    private void renameTypeDecl(TypeDeclaration<?> td, String srcPkg) {
        String simple = td.getNameAsString();
        String srcFqcn = srcPkg.isEmpty() ? simple : srcPkg + "." + simple;
        String dstFqcn = map.classes.get(srcFqcn);
        if (dstFqcn != null) {
            String dstSimple = MapBuilder.simpleName(dstFqcn);
            String oldSimple = td.getNameAsString();
            td.setName(dstSimple);
            // Rename constructors of this type (they must match the new class name)
            if (td instanceof com.github.javaparser.ast.body.ClassOrInterfaceDeclaration) {
                com.github.javaparser.ast.body.ClassOrInterfaceDeclaration cd2 = (com.github.javaparser.ast.body.ClassOrInterfaceDeclaration) td;
                for (com.github.javaparser.ast.body.ConstructorDeclaration ctor : cd2.getConstructors()) {
                    ctor.setName(dstSimple);
                }
                // Update nested 'new OldName(' expressions inside this class body
                cd2.findAll(com.github.javaparser.ast.expr.ObjectCreationExpr.class).forEach(oce -> {
                    if (oce.getTypeAsString().equals(oldSimple)) {
                        oce.setType(dstSimple);
                    }
                });
            }
        }

        if (td instanceof ClassOrInterfaceDeclaration) {
            ClassOrInterfaceDeclaration cd = (ClassOrInterfaceDeclaration) td;
            for (FieldDeclaration f : cd.getFields()) {
                for (VariableDeclarator v : f.getVariables()) {
                    String fieldKey = srcFqcn + "." + v.getNameAsString();
                    String newName = map.fields.get(fieldKey);
                    if (newName != null && !newName.equals(v.getNameAsString())) {
                        renameField(cd, v.getNameAsString(), newName);
                        v.setName(newName);
                    }
                }
            }
            for (MethodDeclaration m : cd.getMethods()) {
                String mKey = srcFqcn + "." + m.getNameAsString();
                String newName = map.methods.get(mKey);
                if (newName != null && !newName.equals(m.getNameAsString())) {
                    String oldName = m.getNameAsString();
                    m.setName(newName);
                    renameMethodCalls(cd, oldName, newName);
                }
            }
        }
    }

    private void renameField(ClassOrInterfaceDeclaration cd, String oldName, String newName) {
        cd.findAll(FieldAccessExpr.class).forEach(fa -> {
            if (fa.getNameAsString().equals(oldName)) fa.setName(newName);
        });
        cd.findAll(NameExpr.class).forEach(n -> {
            if (!n.getNameAsString().equals(oldName)) return;
            if (isShadowedInScope(n, oldName)) return;
            n.setName(newName);
        });
    }

    /** Returns true if a parameter or local variable in the enclosing method/constructor shadows the given name. */
    private boolean isShadowedInScope(com.github.javaparser.ast.Node n, String name) {
        com.github.javaparser.ast.Node cur = n.getParentNode().orElse(null);
        while (cur != null) {
            if (cur instanceof com.github.javaparser.ast.body.CallableDeclaration) {
                com.github.javaparser.ast.body.CallableDeclaration<?> cd = (com.github.javaparser.ast.body.CallableDeclaration<?>) cur;
                for (com.github.javaparser.ast.body.Parameter p : cd.getParameters()) {
                    if (p.getNameAsString().equals(name)) return true;
                }
                // Check local variable declarations inside the body that lexically precede n
                for (com.github.javaparser.ast.body.VariableDeclarator v : cd.findAll(com.github.javaparser.ast.body.VariableDeclarator.class)) {
                    if (v.getNameAsString().equals(name)) {
                        // Only count if v is BEFORE n in source
                        if (v.getRange().isPresent() && n.getRange().isPresent()) {
                            if (v.getRange().get().begin.isBefore(n.getRange().get().begin)) return true;
                        }
                    }
                }
                return false;
            }
            cur = cur.getParentNode().orElse(null);
        }
        return false;
    }

    private void renameMethodCalls(ClassOrInterfaceDeclaration cd, String oldName, String newName) {
        cd.findAll(MethodCallExpr.class).forEach(mc -> {
            if (mc.getNameAsString().equals(oldName) && !mc.getScope().isPresent()) {
                mc.setName(newName);
            }
        });
    }

    private void renameReferences(CompilationUnit cu) {
        // Build a simple-class-name → new simple name map (for fast lookup)
        java.util.Map<String, String> simpleClassMap = new java.util.HashMap<>();
        java.util.Set<String> ambiguousClasses = new java.util.HashSet<>();
        for (Map.Entry<String, String> e : map.classes.entrySet()) {
            String s = MapBuilder.simpleName(e.getKey());
            String n = MapBuilder.simpleName(e.getValue());
            if (s.equals(n)) continue;
            if (simpleClassMap.containsKey(s) && !simpleClassMap.get(s).equals(n)) {
                ambiguousClasses.add(s);
            } else {
                simpleClassMap.put(s, n);
            }
        }
        cu.findAll(ClassOrInterfaceType.class).forEach(t -> {
            String simple = t.getNameAsString();
            if (ambiguousClasses.contains(simple)) return;
            String n = simpleClassMap.get(simple);
            if (n != null) t.setName(n);
        });
        // Also rename NameExpr that match a class (e.g. static call ClassName.method())
        cu.findAll(com.github.javaparser.ast.expr.NameExpr.class).forEach(ne -> {
            String simple = ne.getNameAsString();
            if (ambiguousClasses.contains(simple)) return;
            String n = simpleClassMap.get(simple);
            if (n != null) ne.setName(n);
        });
    }

    private void encryptLiterals(CompilationUnit cu) {
        cu.findAll(StringLiteralExpr.class).forEach(lit -> {
            if (isInsideAnnotation(lit)) return;
            if (lit.getValue().length() < minStringLen) return;
            wrapWithDecryptCall(lit, lit.getValue());
        });
        cu.findAll(TextBlockLiteralExpr.class).forEach(lit -> {
            if (isInsideAnnotation(lit)) return;
            String value = lit.asString();
            if (value.length() < minStringLen) return;
            wrapWithDecryptCall(lit, value);
        });
    }

    private boolean isInsideAnnotation(com.github.javaparser.ast.Node n) {
        com.github.javaparser.ast.Node cur = n.getParentNode().orElse(null);
        while (cur != null) {
            if (cur instanceof AnnotationExpr) return true;
            if (cur instanceof com.github.javaparser.ast.stmt.SwitchEntry) return true;
            // Skip literals used as static final values (constant init)
            if (cur instanceof com.github.javaparser.ast.body.FieldDeclaration) {
                com.github.javaparser.ast.body.FieldDeclaration fd = (com.github.javaparser.ast.body.FieldDeclaration) cur;
                boolean isStatic = fd.getModifiers().stream().anyMatch(m -> m.getKeyword().asString().equals("static"));
                boolean isFinal = fd.getModifiers().stream().anyMatch(m -> m.getKeyword().asString().equals("final"));
                if (isStatic && isFinal) return true;
                return false;
            }
            cur = cur.getParentNode().orElse(null);
        }
        return false;
    }

    private void wrapWithDecryptCall(com.github.javaparser.ast.expr.Expression lit, String value) {
        String enc = XorBase64.encrypt(value, key);
        String simple = helperFqcn.contains(".") ? helperFqcn.substring(helperFqcn.lastIndexOf('.') + 1) : helperFqcn;
        StringBuilder code = new StringBuilder();
        code.append(simple).append(".").append(HelperGenerator.METHOD_NAME).append("(\"");
        for (int i = 0; i < enc.length(); i++) {
            char c = enc.charAt(i);
            if (c == '"' || c == 92) code.append((char)92).append(c);
            else code.append(c);
        }
        code.append("\")");
        try {
            com.github.javaparser.ast.expr.Expression call = StaticJavaParser.parseExpression(code.toString());
            lit.replace(call);
        } catch (Exception e) {
            // skip on parse failure
        }
    }
}
