package printscript.parser.internal.context

import printscript.token.TokenReadResult
import printscript.token.TokenSource
import printscript.token.TokenType

internal data class TokenCursor(
    private val source: TokenSource,
    private val lookahead: TokenReadResult?,
    private val ignoredTokenTypes: Set<TokenType>,
) {

    fun peek(): TokenCursorReadResult {
        val readResult = currentRead()

        return readResult.toCursorRead(
            resultingCursor = withLookahead(readResult),
        )
    }

    fun advance(): TokenCursorReadResult {
        val readResult = currentRead()

        return readResult.toCursorRead(
            resultingCursor = initial(
                source = readResult.remainingSource,
                ignoredTokenTypes = ignoredTokenTypes,
            ),
        )
    }

    private fun currentRead(): TokenReadResult {
        return lookahead ?: readNextRelevantToken(source)
    }

    private tailrec fun readNextRelevantToken(source: TokenSource): TokenReadResult {
        return when (val readResult = source.nextToken()) {
            is TokenReadResult.Failure -> readResult
            is TokenReadResult.Success -> {
                if (readResult.token.type in ignoredTokenTypes) {
                    readNextRelevantToken(readResult.remainingSource)
                } else {
                    readResult
                }
            }
        }
    }

    private fun withLookahead(readResult: TokenReadResult): TokenCursor {
        return copy(lookahead = readResult)
    }

    private fun TokenReadResult.toCursorRead(resultingCursor: TokenCursor): TokenCursorReadResult {
        return when (this) {
            is TokenReadResult.Success -> TokenCursorReadResult.Success(
                token = token,
                resultingCursor = resultingCursor,
            )

            is TokenReadResult.Failure -> TokenCursorReadResult.Failure(
                error = error,
                resultingCursor = resultingCursor,
            )
        }
    }

    companion object {

        fun initial(source: TokenSource, ignoredTokenTypes: Set<TokenType> = emptySet()): TokenCursor {
            return TokenCursor(
                source = source,
                lookahead = null,
                ignoredTokenTypes = ignoredTokenTypes.toSet(),
            )
        }
    }
}
