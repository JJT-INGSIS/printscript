package printscript.parser.internal.statement

import printscript.parser.internal.ParsingResult
import printscript.parser.internal.context.TokenLookahead
import printscript.token.TokenType

internal class StatementMatcher(
    private val expectedTokensByOffset: List<Set<TokenType>>,
) {

    fun matchInitialTokens(
        lookahead: TokenLookahead,
    ): StatementMatch {
        for (offset in expectedTokensByOffset.indices) {
            val expectedTokens = expectedTokensByOffset[offset]

            val token = when (val result = lookahead.peekAt(offset)) {
                is ParsingResult.Success -> result.value
                is ParsingResult.Failure -> return StatementMatch.Failure(result.error)
            }

            if (token.type !in expectedTokens) {
                return StatementMatch.NoMatch(
                    StatementMismatch(
                        lookaheadOffset = offset,
                        expected = expectedTokens,
                        actual = token,
                    ),
                )
            }
        }

        return StatementMatch.Match
    }
}