package printscript.parser.internal.context

import printscript.token.LexicalError
import printscript.token.Token

internal sealed interface TokenCursorReadResult {

    data class Success(
        val token: Token,
        val resultingCursor: TokenCursor,
    ) : TokenCursorReadResult

    data class Failure(
        val error: LexicalError,
    ) : TokenCursorReadResult
}
