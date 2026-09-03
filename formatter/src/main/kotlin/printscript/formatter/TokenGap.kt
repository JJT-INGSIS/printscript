package printscript.formatter

import printscript.token.Token

/** Whitespace found between two significant tokens or at a source boundary. */
public data class TokenGap(
    public val previousToken: Token?,
    public val originalWhitespace: String,
    public val nextToken: Token?,
)
