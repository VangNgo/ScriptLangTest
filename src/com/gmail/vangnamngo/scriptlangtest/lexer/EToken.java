package com.gmail.vangnamngo.scriptlangtest.lexer;

public enum EToken {
    // ========================================================================
    // Unary operators
    // ========================================================================

    NOT,
    ASSIGN,

    // ========================================================================
    // Binary operators
    // ========================================================================

    OPERATOR, // Includes: + - / * % & |
    SET_OPERATOR, // Includes: += -= /= *= %=

    // ========================================================================
    // Comparators
    // ========================================================================

    COMPARATOR, // Includes: == != < <= > >=

    AND,
    OR,

    // ========================================================================
    // Braces
    // ========================================================================

    L_PAREN,
    R_PAREN,
    L_BRACKET,
    R_BRACKET,

    L_BRACE,
    R_BRACE,

    // ========================================================================
    // Comments
    // ========================================================================

    SINGLE_COMMENT,

    FLEX_COMMENT_START,
    FLEX_COMMENT_END,

    // ========================================================================
    // Lang
    // ========================================================================

    HEADER,

    KEYWORD,
    IDENTIFIER,

    CHARACTER,
    STRING,
    BOOLEAN,
    INTEGER,
    DECIMAL,

    NULL,

    // ========================================================================
    // MISC
    // ========================================================================

    DOT,
    COLON,
    COMMA,

    INDENT,
    DEDENT,
    NEWLINE,
    EOF
}
