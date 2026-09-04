package printscript.v1.token

import printscript.token.TokenType

public enum class PrintScriptV1TokenType : TokenType {
    LET,
    CONST,
    NUMBER_TYPE,
    STRING_TYPE,
    BOOLEAN_TYPE,
    PRINTLN,
    IF,
    ELSE,
    READ_INPUT,
    READ_ENV,

    IDENTIFIER,
    NUMBER_LITERAL,
    STRING_LITERAL,
    TRUE,
    FALSE,

    PLUS,
    MINUS,
    STAR,
    SLASH,
    ASSIGN,

    COLON,
    SEMICOLON,
    LEFT_PAREN,
    RIGHT_PAREN,
    LEFT_BRACE,
    RIGHT_BRACE,

    EOF,
}
