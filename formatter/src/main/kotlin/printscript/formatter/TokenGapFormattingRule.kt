package printscript.formatter

import printscript.token.Token

public interface TokenGapFormattingRule {

    public fun supports(gap: TokenGap): Boolean

    public fun formatWhitespace(gap: TokenGap): String

    public fun afterConsuming(token: Token): TokenGapFormattingRule = this
}
