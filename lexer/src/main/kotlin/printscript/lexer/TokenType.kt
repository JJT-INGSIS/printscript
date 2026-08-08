package printscript.lexer

enum class TokenType(
    val fixedLexeme: String? = null) {

    // Keywords
    LET("let"),
    NUMBER_TYPE("number"),
    STRING_TYPE("string"),
    PRINTLN("println"),

    // Dynamic tokens
    IDENTIFIER,
    NUMBER_LITERAL,
    STRING_LITERAL,

    // Operators
    PLUS("+"),
    MINUS("-"),
    STAR("*"),
    SLASH("/"),
    ASSIGN("="),

    // Separators
    COLON(":"),
    SEMICOLON(";"),
    LEFT_PAREN("("),
    RIGHT_PAREN(")"),

    // End of input
    EOF("")
}