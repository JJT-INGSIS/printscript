package printscript.v1.formatter

import printscript.formatter.Formatter
import printscript.formatter.FormatterFactory
import printscript.formatter.TokenGapFormattingRule
import printscript.v1.formatter.configuration.PrintScriptV1FormatterConfiguration
import printscript.v1.formatter.internal.rule.PrintScriptV1FormattingRules
import printscript.v1.token.PrintScriptV1TokenType

public object PrintScriptV1FormatterFactory {

    @JvmStatic
    public fun defaultConfiguration(): PrintScriptV1FormatterConfiguration {
        return PrintScriptV1FormatterConfiguration.default()
    }

    @JvmStatic
    @JvmOverloads
    public fun create(
        configuration: PrintScriptV1FormatterConfiguration = defaultConfiguration(),
        additionalFormattingRules: List<TokenGapFormattingRule> = emptyList(),
    ): Formatter {
        val configuredRules =
            PrintScriptV1FormattingRules.allRules(configuration)

        return FormatterFactory.create(
            formattingRules = additionalFormattingRules + configuredRules,
            whitespaceTokenType = PrintScriptV1TokenType.WHITESPACE,
            endOfInputTokenType = PrintScriptV1TokenType.EOF,
        )
    }
}
