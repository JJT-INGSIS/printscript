package printscript.formatter

import printscript.formatter.internal.ConfigurableFormatter
import printscript.token.TokenType

public object FormatterFactory {

    /**
     * Creates a lazy formatter. When several rules support a gap, the first
     * configured rule has priority. If none supports it, the original
     * whitespace is preserved. [whitespaceTokenType] identifies the tokens
     * that retain that whitespace in the supplied token stream.
     */
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
