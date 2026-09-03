package printscript.formatter

import printscript.token.Token

/** Strategy that may replace the whitespace represented by one [TokenGap]. */
public interface TokenGapFormattingRule {

    public fun supports(gap: TokenGap): Boolean

    public fun formatWhitespace(gap: TokenGap): String

    /**
     * Returns the rule state to use after consuming [token]. Stateless rules
     * keep their current instance; contextual rules return a new immutable one.
     */
    public fun afterConsuming(token: Token): TokenGapFormattingRule = this
}
