package printscript.token

public enum class PrintScriptV1TokenType : TokenType {
    LET,
    NUMBER_TYPE,
    STRING_TYPE,
    PRINTLN,

    IDENTIFIER,
    NUMBER_LITERAL,
    STRING_LITERAL,

    PLUS,
    MINUS,
    STAR,
    SLASH,
    ASSIGN,

    COLON,
    SEMICOLON,
    LEFT_PAREN,
    RIGHT_PAREN,

    EOF,
}
