package printscript.parser.internal.statement

import printscript.token.Token
import printscript.token.TokenType

private const val CURRENT_TOKEN_OFFSET = 0

internal data class StatementMismatch(
    val lookaheadOffset: Int,
    val expected: Set<TokenType>,
    val actual: Token,
) {

    internal companion object {

        fun atCurrentToken(
            expected: Set<TokenType>,
            actual: Token,
        ): StatementMismatch {
            return StatementMismatch(
                lookaheadOffset = CURRENT_TOKEN_OFFSET,
                expected = expected,
                actual = actual,
            )
        }
    }
}