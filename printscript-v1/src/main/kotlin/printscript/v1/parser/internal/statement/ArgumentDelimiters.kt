package printscript.v1.parser.internal.statement

import printscript.token.TokenType

internal data class ArgumentDelimiters(
    val opening: TokenType,
    val closing: TokenType,
)
