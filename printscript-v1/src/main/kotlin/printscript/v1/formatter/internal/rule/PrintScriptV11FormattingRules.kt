package printscript.v1.formatter.internal.rule

import printscript.formatter.TokenGapFormattingRule
import printscript.v1.formatter.configuration.PrintScriptV11FormatterConfiguration
import printscript.v1.formatter.internal.rule.block.IfBlockIndentationRule
import printscript.v1.formatter.internal.rule.block.IfBracePlacementRule

internal object PrintScriptV11FormattingRules {

    fun create(configuration: PrintScriptV11FormatterConfiguration): TokenGapFormattingRule {
        return LayeredFormattingRule(
            lineBreakRules =
            bracePlacementRules(configuration) +
                PrintScriptV1FormattingRules.lineBreakRules(
                    configuration.v1Configuration,
                ),
            inlineSpacingRules =
            PrintScriptV1FormattingRules.spacingRules(
                configuration.v1Configuration,
            ),
            indentationRule =
            indentationRule(configuration),
        )
    }

    private fun bracePlacementRules(
        configuration: PrintScriptV11FormatterConfiguration,
    ): List<TokenGapFormattingRule> {
        val placement =
            configuration.ifBracePlacement
                ?: return emptyList()

        return listOf(
            IfBracePlacementRule(
                placement = placement,
                shouldAlignBraceWithIf =
                configuration.indentationInsideIf == null,
            ),
        )
    }

    private fun indentationRule(configuration: PrintScriptV11FormatterConfiguration): TokenGapFormattingRule? {
        val indentationSize =
            configuration.indentationInsideIf
                ?: return null

        return IfBlockIndentationRule(indentationSize)
    }
}
