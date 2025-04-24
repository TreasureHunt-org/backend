package org.treasurehunt.common.enums;

public enum SupportedLanguages {
    C(4),            // C (Clang 7.0.1)
    C_PLUS_PLUS(10), // C++ (GCC 7.4.0)
    JAVA(91),        // Java (OpenJDK 17.0.6)
    PYTHON(34);      // Python (3.8.1)

    private final int languageId;

    SupportedLanguages(int languageId) {
        this.languageId = languageId;
    }

    public int getLanguageId() {
        return languageId;
    }
}
