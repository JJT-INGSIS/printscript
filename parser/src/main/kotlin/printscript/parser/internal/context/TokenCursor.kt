package printscript.parser.internal.context

import printscript.token.TokenReadResult
import printscript.token.TokenSource

internal data class TokenCursor(
    private val source: TokenSource,
    private val lookahead: TokenReadResult?,
) {

    fun peek(): TokenCursorRead {
        val readResult = currentRead()

        return readResult.toCursorRead(
            resultingCursor = withLookahead(readResult),
        )
    }

    fun advance(): TokenCursorRead {
        val readResult = currentRead()

        return readResult.toCursorRead(
            resultingCursor = initial(readResult.remainingSource),
        )
    }

    private fun currentRead(): TokenReadResult {
        return lookahead ?: source.nextToken()
    }

    private fun withLookahead(
        readResult: TokenReadResult,
    ): TokenCursor {
        return copy(lookahead = readResult)
    }

    private fun TokenReadResult.toCursorRead(
        resultingCursor: TokenCursor,
    ): TokenCursorRead {
        return when (this) {
            is TokenReadResult.Success -> TokenCursorRead.Success(
                token = token,
                resultingCursor = resultingCursor,
            )

            is TokenReadResult.Failure -> TokenCursorRead.Failure(
                error = error,
                resultingCursor = resultingCursor,
            )
        }
    }

    companion object {

        fun initial(
            source: TokenSource,
        ): TokenCursor {
            return TokenCursor(
                source = source,
                lookahead = null,
            )
        }
    }
}