package io.obfuscmapper.parser;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;

public final class ParserConfig {
    private ParserConfig() {}
    public static void init() {
        StaticJavaParser.getParserConfiguration()
            .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17);
    }
}
