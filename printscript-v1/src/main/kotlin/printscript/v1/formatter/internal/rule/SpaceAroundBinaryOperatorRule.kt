package printscript.v1.formatter.internal.rule

import printscript.formatter.TokenGap
import printscript.formatter.TokenGapFormattingRule
import printscript.formatter.WhitespaceFormattingResult
import printscript.token.Token
import printscript.token.TokenType
import printscript.v1.formatter.internal.SPACE
import printscript.v1.token.PrintScriptV1TokenType

internal data class SpaceAroundBinaryOperatorRule(
    private val previousTokenCanEndExpression: Boolean = false,
    private val previousTokenIsBinaryOperator: Boolean = false,
) : TokenGapFormattingRule {

    override fun supports(gap: TokenGap): Boolean {
        val precedesBinaryOperator =
            isBinaryOperator(gap.nextToken?.type) &&
                previousTokenCanEndExpression

        return precedesBinaryOperator || previousTokenIsBinaryOperator
    }

    override fun formatWhitespace(gap: TokenGap): WhitespaceFormattingResult {
        return WhitespaceFormattingResult.Success(SPACE)
    }

    override fun afterConsuming(token: Token): TokenGapFormattingRule {
        val consumedBinaryOperator =
            isBinaryOperator(token.type) &&
                previousTokenCanEndExpression

        return copy(
            previousTokenCanEndExpression = token.type.canEndExpression(),
            previousTokenIsBinaryOperator = consumedBinaryOperator,
        )
    }

    private fun TokenType.canEndExpression(): Boolean {
        return this == PrintScriptV1TokenType.IDENTIFIER ||
            this == PrintScriptV1TokenType.NUMBER_LITERAL ||
            this == PrintScriptV1TokenType.STRING_LITERAL ||
            this == PrintScriptV1TokenType.TRUE ||
            this == PrintScriptV1TokenType.FALSE ||
            this == PrintScriptV1TokenType.RIGHT_PAREN
    }

    private fun isBinaryOperator(tokenType: TokenType?): Boolean {
        return tokenType == PrintScriptV1TokenType.PLUS ||
            tokenType == PrintScriptV1TokenType.MINUS ||
            tokenType == PrintScriptV1TokenType.STAR ||
            tokenType == PrintScriptV1TokenType.SLASH
    }
}
