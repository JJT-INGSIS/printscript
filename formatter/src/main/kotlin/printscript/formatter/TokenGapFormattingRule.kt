package printscript.formatter

import printscript.token.Token

public interface TokenGapFormattingRule {

    public fun supports(gap: TokenGap): Boolean

    public fun formatWhitespace(gap: TokenGap): WhitespaceFormattingResult

    public fun afterFormatting(gap: TokenGap, whitespace: String): TokenGapFormattingRule = this

    public fun afterConsuming(token: Token): TokenGapFormattingRule = this
}
