package printscript.ast

import printscript.model.source.SourceSpan

public data class Identifier(
    public val value: String,
    public val span: SourceSpan,
)
