package printscript.v1.parser.internal.statement

import printscript.token.TokenType

internal data class DeclarationTokens(
    val keyword: TokenType,
    val identifier: TokenType,
    val typeSeparator: TokenType,
    val initializer: TokenType,
)
