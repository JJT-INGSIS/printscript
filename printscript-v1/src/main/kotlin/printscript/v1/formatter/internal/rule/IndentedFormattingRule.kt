package printscript.v1.formatter.internal.rule

import printscript.formatter.TokenGap
import printscript.formatter.TokenGapFormattingRule
import printscript.formatter.WhitespaceFormattingResult
import printscript.token.Token

internal class IndentedFormattingRule(
    lineRules: List<TokenGapFormattingRule>,
    spacingRules: List<TokenGapFormattingRule>,
    private val indentationRule: TokenGapFormattingRule?,
) : TokenGapFormattingRule {

    private val lineRules = lineRules.toList()
    private val spacingRules = spacingRules.toList()

    override fun supports(gap: TokenGap): Boolean = true

    override fun formatWhitespace(gap: TokenGap): WhitespaceFormattingResult {
        val rule = lineRules.firstOrNull { it.supports(gap) }
            ?: spacingRules.firstOrNull { indentationRule?.supports(gap) != true && it.supports(gap) }
        val result = rule?.formatWhitespace(gap) ?: WhitespaceFormattingResult.Success(gap.originalWhitespace)
        return when (result) {
            is WhitespaceFormattingResult.Failure -> result
            is WhitespaceFormattingResult.Success -> indent(gap.copy(originalWhitespace = result.whitespace))
        }
    }

    override fun afterFormatting(gap: TokenGap, whitespace: String): TokenGapFormattingRule {
        return IndentedFormattingRule(
            lineRules.map { it.afterFormatting(gap, whitespace) },
            spacingRules.map { it.afterFormatting(gap, whitespace) },
            indentationRule?.afterFormatting(gap, whitespace),
        )
    }

    override fun afterConsuming(token: Token): TokenGapFormattingRule {
        return IndentedFormattingRule(
            lineRules.map { it.afterConsuming(token) },
            spacingRules.map { it.afterConsuming(token) },
            indentationRule?.afterConsuming(token),
        )
    }

    private fun indent(gap: TokenGap): WhitespaceFormattingResult {
        return if (indentationRule?.supports(gap) == true) {
            indentationRule.formatWhitespace(gap)
        } else {
            WhitespaceFormattingResult.Success(gap.originalWhitespace)
        }
    }
}
