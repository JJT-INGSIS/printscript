package parser.token

/** El DTO que produce el lexer. El parser trabaja siempre contra esto. */
data class Token(
    val type: TokenType,
    val value: String,
    val start: Position,
    val end: Position,
)
