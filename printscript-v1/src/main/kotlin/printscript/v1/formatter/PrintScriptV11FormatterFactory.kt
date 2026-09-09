package printscript.v1.formatter

import printscript.formatter.Formatter
import printscript.formatter.FormatterFactory
import printscript.formatter.TokenGapFormattingRule
import printscript.v1.formatter.internal.configuration.PrintScriptV11FormatterConfigurationReader
import printscript.v1.formatter.internal.rule.IfBlockIndentationRule
import printscript.v1.formatter.internal.rule.IfBracePlacementRule
import printscript.v1.formatter.internal.rule.IndentedFormattingRule
import printscript.v1.token.PrintScriptV1TokenType

public object PrintScriptV11FormatterFactory {

    @JvmStatic
    public fun defaultConfiguration(): PrintScriptV11FormatterConfiguration {
        return PrintScriptV11FormatterConfiguration(
            v1Configuration = PrintScriptV1FormatterFactory.defaultConfiguration(),
        )
    }

    @JvmStatic
    public fun configurationFrom(json: String): PrintScriptV11FormatterConfigurationResult {
        return PrintScriptV11FormatterConfigurationReader.read(json)
    }

    @JvmStatic
    @JvmOverloads
    public fun create(
        configuration: PrintScriptV11FormatterConfiguration = defaultConfiguration(),
        additionalFormattingRules: List<TokenGapFormattingRule> = emptyList(),
    ): Formatter {
        return FormatterFactory.create(
            formattingRules = additionalFormattingRules + printScriptV11FormattingRule(configuration),
            whitespaceTokenType = PrintScriptV1TokenType.WHITESPACE,
            endOfInputTokenType = PrintScriptV1TokenType.EOF,
        )
    }

    private fun printScriptV11FormattingRule(
        configuration: PrintScriptV11FormatterConfiguration,
    ): TokenGapFormattingRule {
        val braceRules = listOfNotNull(
            configuration.ifBracePlacement?.let { placement ->
                IfBracePlacementRule(
                    placement = placement,
                    alignWithIf = configuration.indentationInsideIf == null,
                )
            },
        )
        return IndentedFormattingRule(
            lineRules = braceRules + PrintScriptV1FormatterFactory.lineBreakRules(configuration.v1Configuration),
            spacingRules = PrintScriptV1FormatterFactory.spacingRules(configuration.v1Configuration),
            indentationRule = configuration.indentationInsideIf?.let(::IfBlockIndentationRule),
        )
    }
}
