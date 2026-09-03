package printscript.formatter

import printscript.formatter.internal.ConfigurableFormatter
import printscript.token.TokenType

public object FormatterFactory {

    public fun create(
        formattingRules: List<TokenGapFormattingRule>,
        whitespaceTokenType: TokenType,
        endOfInputTokenType: TokenType,
    ): Formatter {
        return ConfigurableFormatter(
            formattingRules = formattingRules,
            whitespaceTokenType = whitespaceTokenType,
            endOfInputTokenType = endOfInputTokenType,
        )
    }
}
