package printscript.token

import printscript.model.source.SourceSpan

public data class Token(
    public val type: TokenType,
    public val lexeme: String,
    public val span: SourceSpan,
)
