package printscript.formatter.internal

import printscript.formatter.FormattedSource
import printscript.formatter.Formatter
import printscript.formatter.TokenGapFormattingRule
import printscript.formatter.internal.rule.TokenGapFormattingRuleDispatcher
import printscript.token.TokenSource
import printscript.token.TokenType

internal class ConfigurableFormatter(
    formattingRules: List<TokenGapFormattingRule>,
    private val whitespaceTokenType: TokenType,
    private val endOfInputTokenType: TokenType,
) : Formatter {

    private val ruleDispatcher = TokenGapFormattingRuleDispatcher(
        formattingRules = formattingRules,
    )

    override fun format(tokenSource: TokenSource): FormattedSource {
        return TokenFormattingSource(
            tokenSource = tokenSource,
            ruleDispatcher = ruleDispatcher,
            gapReader = TokenGapReader(
                whitespaceTokenType = whitespaceTokenType,
                endOfInputTokenType = endOfInputTokenType,
            ),
            previousToken = null,
            reachedEndOfInput = false,
        )
    }
}
