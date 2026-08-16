package printscript.parser.internal.statement

import printscript.token.Token
import printscript.token.TokenType

internal data class StatementMismatch(
    val lookaheadOffset: Int,
    val expected: Set<TokenType>,
    val actual: Token,
)