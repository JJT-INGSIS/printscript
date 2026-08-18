package printscript.ast

import printscript.model.source.SourceSpan

interface Node {
    val span: SourceSpan
}