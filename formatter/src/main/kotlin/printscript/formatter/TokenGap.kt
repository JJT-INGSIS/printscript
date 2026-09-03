package printscript.formatter

import printscript.token.Token

public data class TokenGap(
    public val previousToken: Token?,
    public val originalWhitespace: String,
    public val nextToken: Token?,
)
