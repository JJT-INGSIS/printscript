package printscript.v1.formatter.internal.rule

import printscript.formatter.TokenGap
import printscript.formatter.TokenGapFormattingRule
import printscript.formatter.WhitespaceFormattingResult
import printscript.token.Token

internal class CompositeFormattingRule(
    structuralRules: List<TokenGapFormattingRule>,
    spacingRules: List<TokenGapFormattingRule>,
    postProcessingRules: List<TokenGapFormattingRule>,
) : TokenGapFormattingRule {

    private val structuralRules = structuralRules.toList()
    private val spacingRules = spacingRules.toList()
    private val postProcessingRules = postProcessingRules.toList()

    override fun supports(gap: TokenGap): Boolean = true

    override fun formatWhitespace(gap: TokenGap): WhitespaceFormattingResult {
        return when (val initialFormatting = formatInitialWhitespace(gap)) {
            is WhitespaceFormattingResult.Failure -> initialFormatting
            is WhitespaceFormattingResult.Success ->
                applyPostProcessingRules(
                    gap = gap,
                    initialWhitespace = initialFormatting.whitespace,
                )
        }
    }

    override fun afterFormatting(gap: TokenGap, whitespace: String): TokenGapFormattingRule {
        return CompositeFormattingRule(
            structuralRules = structuralRules.map { it.afterFormatting(gap, whitespace) },
            spacingRules = spacingRules.map { it.afterFormatting(gap, whitespace) },
            postProcessingRules = postProcessingRules.map { it.afterFormatting(gap, whitespace) },
        )
    }

    override fun afterConsuming(token: Token): TokenGapFormattingRule {
        return CompositeFormattingRule(
            structuralRules = structuralRules.map { it.afterConsuming(token) },
            spacingRules = spacingRules.map { it.afterConsuming(token) },
            postProcessingRules = postProcessingRules.map { it.afterConsuming(token) },
        )
    }

    private fun formatInitialWhitespace(gap: TokenGap): WhitespaceFormattingResult {
        val structuralRule = firstSupportingRule(structuralRules, gap)

        if (structuralRule != null) {
            return structuralRule.formatWhitespace(gap)
        }

        if (hasApplicablePostProcessingRule(gap)) {
            return keepOriginalWhitespace(gap)
        }

        val spacingRule = firstSupportingRule(spacingRules, gap)

        return spacingRule?.formatWhitespace(gap)
            ?: keepOriginalWhitespace(gap)
    }

    private fun applyPostProcessingRules(gap: TokenGap, initialWhitespace: String): WhitespaceFormattingResult {
        var currentWhitespace = initialWhitespace

        for (rule in postProcessingRules) {
            val currentGap = gap.copy(originalWhitespace = currentWhitespace)

            if (!rule.supports(currentGap)) {
                continue
            }

            when (val formatting = rule.formatWhitespace(currentGap)) {
                is WhitespaceFormattingResult.Failure -> return formatting
                is WhitespaceFormattingResult.Success ->
                    currentWhitespace = formatting.whitespace
            }
        }

        return WhitespaceFormattingResult.Success(currentWhitespace)
    }

    private fun hasApplicablePostProcessingRule(gap: TokenGap): Boolean {
        return postProcessingRules.any { it.supports(gap) }
    }

    private fun keepOriginalWhitespace(gap: TokenGap): WhitespaceFormattingResult {
        return WhitespaceFormattingResult.Success(gap.originalWhitespace)
    }

    private fun firstSupportingRule(rules: List<TokenGapFormattingRule>, gap: TokenGap): TokenGapFormattingRule? {
        return rules.firstOrNull { it.supports(gap) }
    }
}
