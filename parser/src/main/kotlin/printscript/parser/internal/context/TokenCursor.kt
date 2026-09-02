package printscript.parser.internal.context

import printscript.token.TokenReadResult
import printscript.token.TokenSource

internal data class TokenCursor(
    private val source: TokenSource,
    private val lookahead: TokenReadResult?,
) {

    fun peek(): TokenCursorReadResult {
        return read { readResult -> withLookahead(readResult) }
    }

    fun advance(): TokenCursorReadResult {
        return read { readResult -> initial(readResult.remainingSource) }
    }

    private inline fun read(successorCursor: (TokenReadResult.Success) -> TokenCursor): TokenCursorReadResult {
        return when (val readResult = currentRead()) {
            is TokenReadResult.Success -> TokenCursorReadResult.Success(
                token = readResult.token,
                resultingCursor = successorCursor(readResult),
            )

            is TokenReadResult.Failure -> TokenCursorReadResult.Failure(
                error = readResult.error,
            )
        }
    }

    private fun currentRead(): TokenReadResult {
        return lookahead ?: source.nextToken()
    }

    private fun withLookahead(readResult: TokenReadResult): TokenCursor {
        return copy(lookahead = readResult)
    }

    companion object {

        fun initial(source: TokenSource): TokenCursor {
            return TokenCursor(
                source = source,
                lookahead = null,
            )
        }
    }
}
