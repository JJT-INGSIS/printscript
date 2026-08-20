package printscript.parser.internal.context

import printscript.token.LexicalError
import printscript.token.Token

internal sealed interface TokenCursorRead {

    val resultingCursor: TokenCursor

    data class Success(
        val token: Token,
        override val resultingCursor: TokenCursor,
    ) : TokenCursorRead

    data class Failure(
        val error: LexicalError,
        override val resultingCursor: TokenCursor,
    ) : TokenCursorRead
}