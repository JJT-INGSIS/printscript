package printscript.formatter.internal

import printscript.formatter.TokenGap
import printscript.token.Token
import printscript.token.TokenReadError
import printscript.token.TokenReadResult
import printscript.token.TokenSource
import printscript.token.TokenType

internal class TokenGapReader(
    private val whitespaceTokenType: TokenType,
    private val endOfInputTokenType: TokenType,
) {

    fun readNextGap(tokenSource: TokenSource, previousToken: Token?): TokenGapReadResult {
        return readUntilNextToken(
            source = tokenSource,
            previousToken = previousToken,
            accumulatedWhitespace = "",
        )
    }

    private tailrec fun readUntilNextToken(
        source: TokenSource,
        previousToken: Token?,
        accumulatedWhitespace: String,
    ): TokenGapReadResult {
        return when (val tokenReadResult = source.nextToken()) {
            is TokenReadResult.Failure ->
                TokenGapReadResult.Failure(tokenReadResult.error)

            is TokenReadResult.Success -> {
                when (tokenReadResult.token.type) {
                    endOfInputTokenType ->
                        createGapReadSuccess(
                            previousToken = previousToken,
                            whitespace = accumulatedWhitespace,
                            nextToken = null,
                            remainingTokenSource = tokenReadResult.remainingSource,
                        )

                    whitespaceTokenType ->
                        readUntilNextToken(
                            source = tokenReadResult.remainingSource,
                            previousToken = previousToken,
                            accumulatedWhitespace = accumulatedWhitespace + tokenReadResult.token.lexeme,
                        )

                    else ->
                        createGapReadSuccess(
                            previousToken = previousToken,
                            whitespace = accumulatedWhitespace,
                            nextToken = tokenReadResult.token,
                            remainingTokenSource = tokenReadResult.remainingSource,
                        )
                }
            }
        }
    }

    private fun createGapReadSuccess(
        previousToken: Token?,
        whitespace: String,
        nextToken: Token?,
        remainingTokenSource: TokenSource,
    ): TokenGapReadResult.Success {
        return TokenGapReadResult.Success(
            gap = TokenGap(
                previousToken = previousToken,
                originalWhitespace = whitespace,
                nextToken = nextToken,
            ),
            remainingTokenSource = remainingTokenSource,
        )
    }
}

internal sealed interface TokenGapReadResult {

    data class Success(
        val gap: TokenGap,
        val remainingTokenSource: TokenSource,
    ) : TokenGapReadResult

    data class Failure(
        val error: TokenReadError,
    ) : TokenGapReadResult
}
