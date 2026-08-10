package printscript.model.ast

import printscript.model.source.SourceSpan

interface Node {
    val span: SourceSpan
}