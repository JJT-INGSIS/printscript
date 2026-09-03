package printscript.ast.expression

import printscript.model.source.SourceSpan

public sealed interface Expression {

    public val span: SourceSpan
}
