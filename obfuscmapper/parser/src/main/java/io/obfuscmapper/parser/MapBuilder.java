package io.obfuscmapper.parser;

import java.util.*;

/**
 * Builds an ObfuscationMap from collected symbols, applying NamingScheme + preservation rules.
 */
public class MapBuilder {

    private static final Set<String> NEVER_RENAME_PREFIXES = Set.of(
        "java", "javax", "jakarta", "org.springframework", "org.junit", "org.apache",
        "org.hibernate", "org.slf4j", "org.w3c", "com.fasterxml", "com.google", "lombok",
        "groovyjarjarantlr4"
    );

    public static ObfuscationMap build(SymbolCollector.Collected col, String seed, String preservedTopPackage) {
        ObfuscationMap map = new ObfuscationMap();
        NamingScheme ns = new NamingScheme(seed);

        for (String fqcn : col.preserveClasses) {
            String simple = simpleName(fqcn);
            ns.reserve(simple);
        }

        Map<String, String> partRename = new LinkedHashMap<>();
        for (String pkg : col.packages) {
            if (isNeverRenamed(pkg)) {
                map.packages.put(pkg, pkg);
                continue;
            }
            String obfPkg = renamePackage(pkg, preservedTopPackage, partRename, ns);
            map.packages.put(pkg, obfPkg);
        }

        for (String fqcn : col.classes) {
            String pkg = packageOf(fqcn);
            String simple = simpleName(fqcn);
            String obfPkg = map.packages.getOrDefault(pkg, pkg);
            String obfSimple;
            if (col.preserveClasses.contains(fqcn)) {
                obfSimple = simple;
            } else {
                obfSimple = ns.forClass(fqcn);
            }
            String obfFqcn = obfPkg.isEmpty() ? obfSimple : obfPkg + "." + obfSimple;
            map.classes.put(fqcn, obfFqcn);
        }

        for (String f : col.fields) {
            map.fields.put(f, ns.forField(f));
        }

        for (String m : col.methods) {
            if (col.preserveMethods.contains(m)) {
                map.methods.put(m, simpleName(m));
            } else {
                map.methods.put(m, ns.forMethod(m));
            }
        }

        for (Map.Entry<String, String> e : col.fileToPrimary.entrySet()) {
            String relSrc = e.getKey();
            String fqcn = e.getValue();
            String obfFqcn = map.classes.get(fqcn);
            if (obfFqcn == null) continue;
            String relTgt = "src/main/java/" + obfFqcn.replace('.', '/') + ".java";
            map.filePathRenames.put(relSrc, relTgt);
        }

        return map;
    }

    private static boolean isNeverRenamed(String pkg) {
        for (String pref : NEVER_RENAME_PREFIXES) {
            if (pkg.equals(pref) || pkg.startsWith(pref + ".")) return true;
        }
        return false;
    }

    private static String renamePackage(String pkg, String preservedPrefix, Map<String, String> partCache, NamingScheme ns) {
        String prefix = "";
        String rest = pkg;
        if (preservedPrefix != null && !preservedPrefix.isEmpty()) {
            if (pkg.equals(preservedPrefix)) return pkg;
            if (pkg.startsWith(preservedPrefix + ".")) {
                prefix = preservedPrefix + ".";
                rest = pkg.substring(prefix.length());
            }
        }
        String[] parts = rest.split(java.util.regex.Pattern.quote("."));
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String p = parts[i];
            String r = partCache.computeIfAbsent(p, ns::forPackagePart);
            if (i > 0) out.append('.');
            out.append(r);
        }
        return prefix + out.toString();
    }

    public static String packageOf(String fqcn) {
        int i = fqcn.lastIndexOf('.');
        return i < 0 ? "" : fqcn.substring(0, i);
    }

    public static String simpleName(String fqcn) {
        int i = fqcn.lastIndexOf('.');
        return i < 0 ? fqcn : fqcn.substring(i + 1);
    }
}
