package printscript.v1.formatter

import printscript.formatter.Formatter
import printscript.formatter.FormatterFactory
import printscript.formatter.TokenGapFormattingRule
import printscript.v1.formatter.configuration.PrintScriptV11FormatterConfiguration
import printscript.v1.formatter.internal.rule.PrintScriptV11FormattingRules
import printscript.v1.token.PrintScriptV1TokenType

public object PrintScriptV11FormatterFactory {

    @JvmStatic
    public fun defaultConfiguration(): PrintScriptV11FormatterConfiguration {
        return PrintScriptV11FormatterConfiguration.default()
    }

    @JvmStatic
    @JvmOverloads
    public fun create(
        configuration: PrintScriptV11FormatterConfiguration =
            defaultConfiguration(),
        additionalFormattingRules: List<TokenGapFormattingRule> =
            emptyList(),
    ): Formatter {
        val configuredRule =
            PrintScriptV11FormattingRules.create(configuration)

        return FormatterFactory.create(
            formattingRules =
            additionalFormattingRules + configuredRule,
            whitespaceTokenType =
            PrintScriptV1TokenType.WHITESPACE,
            endOfInputTokenType =
            PrintScriptV1TokenType.EOF,
        )
    }
}
