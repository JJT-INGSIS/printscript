package printscript.formatter.internal.rule

import printscript.formatter.TokenGap
import printscript.formatter.TokenGapFormattingRule
import printscript.token.Token

internal class TokenGapFormattingRuleDispatcher(
    formattingRules: List<TokenGapFormattingRule>,
) {

    private val formattingRules: List<TokenGapFormattingRule> = formattingRules.toList()

    fun formatWhitespace(gap: TokenGap): String {
        val rule = formattingRules.firstOrNull { candidate ->
            candidate.supports(gap)
        }

        return rule?.formatWhitespace(gap) ?: gap.originalWhitespace
    }

    fun afterConsuming(token: Token): TokenGapFormattingRuleDispatcher {
        return TokenGapFormattingRuleDispatcher(
            formattingRules = formattingRules.map { rule ->
                rule.afterConsuming(token)
            },
        )
    }
}
