package printscript.parser.internal.context

import printscript.token.Token
import printscript.token.TokenReadError

internal sealed interface TokenCursorReadResult {

    val resultingCursor: TokenCursor

    data class Success(
        val token: Token,
        override val resultingCursor: TokenCursor,
    ) : TokenCursorReadResult

    data class Failure(
        val error: TokenReadError,
        override val resultingCursor: TokenCursor,
    ) : TokenCursorReadResult
}
