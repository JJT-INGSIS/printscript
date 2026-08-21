package printscript.ast

import printscript.model.source.SourceSpan

public interface Node {

    public val span: SourceSpan
}
