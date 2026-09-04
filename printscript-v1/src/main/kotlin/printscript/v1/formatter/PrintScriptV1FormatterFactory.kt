package printscript.v1.formatter

import printscript.formatter.Formatter
import printscript.formatter.FormatterFactory
import printscript.formatter.TokenGapFormattingRule
import printscript.v1.formatter.internal.configuration.PrintScriptV1FormatterConfigurationReader
import printscript.v1.formatter.internal.rule.EqualsSpacingRule
import printscript.v1.formatter.internal.rule.LineBreakAfterPrintlnRule
import printscript.v1.formatter.internal.rule.LineBreakAfterStatementRule
import printscript.v1.formatter.internal.rule.SingleSpaceSeparationRule
import printscript.v1.formatter.internal.rule.SpaceAfterDeclarationColonRule
import printscript.v1.formatter.internal.rule.SpaceAroundBinaryOperatorRule
import printscript.v1.formatter.internal.rule.SpaceBeforeDeclarationColonRule
import printscript.v1.lexer.PrintScriptV1FormattingTokenType
import printscript.v1.token.PrintScriptV1TokenType

public object PrintScriptV1FormatterFactory {

    @JvmStatic
    public fun defaultConfiguration(): PrintScriptV1FormatterConfiguration {
        return PrintScriptV1FormatterConfiguration()
    }

    @JvmStatic
    public fun configurationFrom(json: String): PrintScriptV1FormatterConfigurationResult {
        return PrintScriptV1FormatterConfigurationReader.read(json)
    }

    @JvmStatic
    @JvmOverloads
    public fun create(
        configuration: PrintScriptV1FormatterConfiguration = defaultConfiguration(),
        additionalFormattingRules: List<TokenGapFormattingRule> = emptyList(),
    ): Formatter {
        return FormatterFactory.create(
            formattingRules =
            additionalFormattingRules +
                printScriptV1FormattingRules(configuration),
            whitespaceTokenType = PrintScriptV1FormattingTokenType.WHITESPACE,
            endOfInputTokenType = PrintScriptV1TokenType.EOF,
        )
    }

    private fun printScriptV1FormattingRules(
        configuration: PrintScriptV1FormatterConfiguration,
    ): List<TokenGapFormattingRule> {
        return listOfNotNull(
            configuration.lineBreaksAfterPrintln?.let { blankLineCount ->
                LineBreakAfterPrintlnRule(blankLineCount)
            },
            LineBreakAfterStatementRule.takeIf {
                configuration.enforceLineBreakAfterStatement
            },
            configuration.equalsSpacing?.let(::EqualsSpacingRule),
            SpaceBeforeDeclarationColonRule.takeIf {
                configuration.enforceSpaceBeforeColonInDeclaration
            },
            SpaceAfterDeclarationColonRule.takeIf {
                configuration.enforceSpaceAfterColonInDeclaration
            },
            if (configuration.enforceSpaceAroundBinaryOperators) {
                SpaceAroundBinaryOperatorRule()
            } else {
                null
            },
            SingleSpaceSeparationRule.takeIf {
                configuration.enforceSingleSpaceSeparation
            },
        )
    }
}
