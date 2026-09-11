package printscript.v1.formatter.internal.rule.spacing

import printscript.formatter.TokenGap
import printscript.formatter.TokenGapFormattingRule
import printscript.formatter.WhitespaceFormattingResult
import printscript.token.Token
import printscript.token.TokenType
import printscript.v1.formatter.internal.whitespace.SPACE
import printscript.v1.token.PrintScriptV1TokenType

internal data class SpaceAroundBinaryOperatorRule(
    private val lastTokenCanEndExpression: Boolean = false,
    private val lastTokenWasBinaryOperator: Boolean = false,
) : TokenGapFormattingRule {

    override fun supports(gap: TokenGap): Boolean {
        val gapPrecedesBinaryOperator =
            gap.nextToken?.type.isBinaryOperator() && lastTokenCanEndExpression

        return gapPrecedesBinaryOperator || lastTokenWasBinaryOperator
    }

    override fun formatWhitespace(gap: TokenGap): WhitespaceFormattingResult {
        return WhitespaceFormattingResult.Success(SPACE)
    }

    override fun afterConsuming(token: Token): TokenGapFormattingRule {
        val consumedTokenIsBinaryOperator =
            token.type.isBinaryOperator() && lastTokenCanEndExpression

        return copy(
            lastTokenCanEndExpression = token.type.canEndExpression(),
            lastTokenWasBinaryOperator = consumedTokenIsBinaryOperator,
        )
    }

    private fun TokenType.canEndExpression(): Boolean {
        return when (this) {
            PrintScriptV1TokenType.IDENTIFIER,
            PrintScriptV1TokenType.NUMBER_LITERAL,
            PrintScriptV1TokenType.STRING_LITERAL,
            PrintScriptV1TokenType.TRUE,
            PrintScriptV1TokenType.FALSE,
            PrintScriptV1TokenType.RIGHT_PAREN,
            -> true

            else -> false
        }
    }

    private fun TokenType?.isBinaryOperator(): Boolean {
        return when (this) {
            PrintScriptV1TokenType.PLUS,
            PrintScriptV1TokenType.MINUS,
            PrintScriptV1TokenType.STAR,
            PrintScriptV1TokenType.SLASH,
            -> true

            else -> false
        }
    }
}
