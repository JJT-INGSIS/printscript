package printscript.v1.formatter.internal.rule

import printscript.formatter.TokenGapFormattingRule
import printscript.v1.formatter.configuration.PrintScriptV11FormatterConfiguration
import printscript.v1.formatter.internal.rule.block.IfBlockIndentationRule
import printscript.v1.formatter.internal.rule.block.IfBracePlacementRule
import printscript.v1.formatter.internal.rule.linebreak.LineBreakInsideBlockRule

internal object PrintScriptV11FormattingRules {

    fun create(configuration: PrintScriptV11FormatterConfiguration): TokenGapFormattingRule {
        return CompositeFormattingRule(
            structuralRules = structuralRules(configuration),
            spacingRules = PrintScriptV1FormattingRules.spacingRules(
                configuration.v1Configuration,
            ),
            postProcessingRules = listOfNotNull(
                indentationRule(configuration),
            ),
        )
    }

    private fun structuralRules(configuration: PrintScriptV11FormatterConfiguration): List<TokenGapFormattingRule> {
        return PrintScriptV1FormattingRules.structuralRules(
            configuration.v1Configuration,
        ) + listOfNotNull(
            bracePlacementRule(configuration),
            lineBreakInsideBlockRule(configuration),
        )
    }

    private fun bracePlacementRule(configuration: PrintScriptV11FormatterConfiguration): TokenGapFormattingRule? {
        val placement =
            configuration.ifBracePlacement
                ?: return null

        return IfBracePlacementRule(
            placement = placement,
            shouldAlignBraceWithIf = configuration.indentationInsideIf == null,
        )
    }

    private fun lineBreakInsideBlockRule(
        configuration: PrintScriptV11FormatterConfiguration,
    ): TokenGapFormattingRule? {
        if (!configuration.enforceLineBreaksInsideIf) {
            return null
        }

        return LineBreakInsideBlockRule
    }

    private fun indentationRule(configuration: PrintScriptV11FormatterConfiguration): TokenGapFormattingRule? {
        val indentationSize =
            configuration.indentationInsideIf
                ?: return null

        return IfBlockIndentationRule(indentationSize)
    }
}
