package printscript.formatter.internal.rule

import printscript.formatter.TokenGap
import printscript.formatter.TokenGapFormattingRule
import printscript.formatter.WhitespaceFormattingResult

internal class TokenGapFormattingRuleDispatcher(
    formattingRules: List<TokenGapFormattingRule>,
) {

    private val formattingRules: List<TokenGapFormattingRule> = formattingRules.toList()

    fun formatWhitespace(gap: TokenGap): WhitespaceFormattingResult {
        val rule = formattingRules.firstOrNull { candidate ->
            candidate.supports(gap)
        }

        return rule?.formatWhitespace(gap) ?: WhitespaceFormattingResult.Success(gap.originalWhitespace)
    }

    fun afterFormatting(gap: TokenGap, whitespace: String): TokenGapFormattingRuleDispatcher {
        return TokenGapFormattingRuleDispatcher(
            formattingRules = formattingRules.map { rule ->
                val updatedRule = rule.afterFormatting(gap, whitespace)
                gap.nextToken?.let(updatedRule::afterConsuming) ?: updatedRule
            },
        )
    }
}
