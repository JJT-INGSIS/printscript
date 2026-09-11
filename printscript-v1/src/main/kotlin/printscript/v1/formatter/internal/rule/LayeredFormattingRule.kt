package printscript.v1.formatter.internal.rule

import printscript.formatter.TokenGap
import printscript.formatter.TokenGapFormattingRule
import printscript.formatter.WhitespaceFormattingResult
import printscript.token.Token

internal class LayeredFormattingRule(
    lineBreakRules: List<TokenGapFormattingRule>,
    inlineSpacingRules: List<TokenGapFormattingRule>,
    private val indentationRule: TokenGapFormattingRule?,
) : TokenGapFormattingRule {

    private val lineBreakRules = lineBreakRules.toList()
    private val inlineSpacingRules = inlineSpacingRules.toList()

    override fun supports(gap: TokenGap): Boolean = true

    override fun formatWhitespace(gap: TokenGap): WhitespaceFormattingResult {
        val baseFormatting = applyBaseFormatting(gap)
        return applyIndentation(gap, baseFormatting)
    }

    override fun afterFormatting(gap: TokenGap, whitespace: String): TokenGapFormattingRule {
        return LayeredFormattingRule(
            lineBreakRules = updateRulesAfterFormatting(lineBreakRules, gap, whitespace),
            inlineSpacingRules = updateRulesAfterFormatting(inlineSpacingRules, gap, whitespace),
            indentationRule = indentationRule?.afterFormatting(gap, whitespace),
        )
    }

    override fun afterConsuming(token: Token): TokenGapFormattingRule {
        return LayeredFormattingRule(
            lineBreakRules = updateRulesAfterConsuming(lineBreakRules, token),
            inlineSpacingRules = updateRulesAfterConsuming(inlineSpacingRules, token),
            indentationRule = indentationRule?.afterConsuming(token),
        )
    }

    private fun applyBaseFormatting(gap: TokenGap): WhitespaceFormattingResult {
        val lineBreakRule = firstRuleSupporting(lineBreakRules, gap)
        if (lineBreakRule != null) {
            return lineBreakRule.formatWhitespace(gap)
        }

        if (indentationRule?.supports(gap) == true) {
            return WhitespaceFormattingResult.Success(gap.originalWhitespace)
        }

        val spacingRule = firstRuleSupporting(inlineSpacingRules, gap)
        return spacingRule?.formatWhitespace(gap)
            ?: WhitespaceFormattingResult.Success(gap.originalWhitespace)
    }

    private fun applyIndentation(
        originalGap: TokenGap,
        initialFormatting: WhitespaceFormattingResult,
    ): WhitespaceFormattingResult {
        return when (initialFormatting) {
            is WhitespaceFormattingResult.Failure -> initialFormatting
            is WhitespaceFormattingResult.Success -> applyIndentationToSuccessfulFormatting(
                originalGap = originalGap,
                formattedWhitespace = initialFormatting.whitespace,
            )
        }
    }

    private fun applyIndentationToSuccessfulFormatting(
        originalGap: TokenGap,
        formattedWhitespace: String,
    ): WhitespaceFormattingResult {
        val formattedGap = originalGap.copy(originalWhitespace = formattedWhitespace)

        if (indentationRule?.supports(formattedGap) != true) {
            return WhitespaceFormattingResult.Success(formattedWhitespace)
        }

        return indentationRule.formatWhitespace(formattedGap)
    }

    private fun firstRuleSupporting(rules: List<TokenGapFormattingRule>, gap: TokenGap): TokenGapFormattingRule? {
        return rules.firstOrNull { it.supports(gap) }
    }

    private fun updateRulesAfterFormatting(
        rules: List<TokenGapFormattingRule>,
        gap: TokenGap,
        whitespace: String,
    ): List<TokenGapFormattingRule> {
        return rules.map { it.afterFormatting(gap, whitespace) }
    }

    private fun updateRulesAfterConsuming(
        rules: List<TokenGapFormattingRule>,
        token: Token,
    ): List<TokenGapFormattingRule> {
        return rules.map { it.afterConsuming(token) }
    }
}
