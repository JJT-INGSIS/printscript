package printscript.v1.formatter.internal.rule

import printscript.formatter.TokenGapFormattingRule
import printscript.v1.formatter.configuration.PrintScriptV1FormatterConfiguration
import printscript.v1.formatter.internal.rule.linebreak.LineBreakAfterPrintlnRule
import printscript.v1.formatter.internal.rule.linebreak.LineBreakAfterStatementRule
import printscript.v1.formatter.internal.rule.spacing.DeclarationColonSpacingRule
import printscript.v1.formatter.internal.rule.spacing.EqualsSpacingRule
import printscript.v1.formatter.internal.rule.spacing.SingleSpaceSeparationRule
import printscript.v1.formatter.internal.rule.spacing.SpaceAroundBinaryOperatorRule

internal object PrintScriptV1FormattingRules {

    fun allRules(configuration: PrintScriptV1FormatterConfiguration): List<TokenGapFormattingRule> {
        return lineBreakRules(configuration) +
            spacingRules(configuration)
    }

    fun lineBreakRules(configuration: PrintScriptV1FormatterConfiguration): List<TokenGapFormattingRule> {
        return listOfNotNull(
            lineBreakAfterPrintlnRule(configuration),
            lineBreakAfterStatementRule(configuration),
        )
    }

    fun spacingRules(configuration: PrintScriptV1FormatterConfiguration): List<TokenGapFormattingRule> {
        return listOfNotNull(
            equalsSpacingRule(configuration),
            declarationColonSpacingRule(configuration),
            binaryOperatorSpacingRule(configuration),
            singleSpaceSeparationRule(configuration),
        )
    }

    private fun lineBreakAfterPrintlnRule(
        configuration: PrintScriptV1FormatterConfiguration,
    ): TokenGapFormattingRule? {
        val blankLineCount =
            configuration.blankLinesAfterPrintln
                ?: return null

        return LineBreakAfterPrintlnRule(blankLineCount)
    }

    private fun lineBreakAfterStatementRule(
        configuration: PrintScriptV1FormatterConfiguration,
    ): TokenGapFormattingRule? {
        if (!configuration.enforceLineBreakAfterStatement) {
            return null
        }

        return LineBreakAfterStatementRule
    }

    private fun equalsSpacingRule(configuration: PrintScriptV1FormatterConfiguration): TokenGapFormattingRule? {
        val spacing =
            configuration.equalsSpacing
                ?: return null

        return EqualsSpacingRule(spacing)
    }

    private fun declarationColonSpacingRule(
        configuration: PrintScriptV1FormatterConfiguration,
    ): TokenGapFormattingRule? {
        val enforceSpaceBefore =
            configuration.enforceSpaceBeforeColonInDeclaration

        val enforceSpaceAfter =
            configuration.enforceSpaceAfterColonInDeclaration

        if (!enforceSpaceBefore && !enforceSpaceAfter) {
            return null
        }

        return DeclarationColonSpacingRule(
            enforceSpaceBeforeColon = enforceSpaceBefore,
            enforceSpaceAfterColon = enforceSpaceAfter,
        )
    }

    private fun binaryOperatorSpacingRule(
        configuration: PrintScriptV1FormatterConfiguration,
    ): TokenGapFormattingRule? {
        if (!configuration.enforceSpaceAroundBinaryOperators) {
            return null
        }

        return SpaceAroundBinaryOperatorRule()
    }

    private fun singleSpaceSeparationRule(
        configuration: PrintScriptV1FormatterConfiguration,
    ): TokenGapFormattingRule? {
        if (!configuration.enforceSingleSpaceSeparation) {
            return null
        }

        return SingleSpaceSeparationRule
    }
}
