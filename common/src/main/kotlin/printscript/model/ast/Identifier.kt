package printscript.model.ast

import printscript.model.source.SourceSpan

data class Identifier(
    val value: String,
    override val span: SourceSpan,
) : Node