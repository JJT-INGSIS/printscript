package printscript.v1.formatter

import printscript.formatter.Formatter
import printscript.formatter.TokenGapFormattingRule
import printscript.v1.formatter.internal.configuration.PrintScriptV11FormatterConfigurationReader
import printscript.v1.formatter.internal.rule.IfBlockIndentationRule
import printscript.v1.formatter.internal.rule.IfBracePlacementRule

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
        return PrintScriptV1FormatterFactory.create(
            configuration = configuration.v1Configuration,
            additionalFormattingRules =
            additionalFormattingRules + printScriptV11FormattingRules(configuration),
        )
    }

    private fun printScriptV11FormattingRules(
        configuration: PrintScriptV11FormatterConfiguration,
    ): List<TokenGapFormattingRule> {
        return listOfNotNull(
            configuration.ifBracePlacement?.let { placement ->
                IfBracePlacementRule(
                    placement = placement,
                    indentationSize = configuration.indentationInsideIf,
                )
            },
            configuration.indentationInsideIf?.let(::IfBlockIndentationRule),
        )
    }
}
