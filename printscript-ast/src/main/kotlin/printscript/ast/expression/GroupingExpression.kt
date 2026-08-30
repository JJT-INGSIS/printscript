package printscript.ast.expression

import printscript.model.source.SourceSpan

public data class GroupingExpression(
    public val expression: Expression,
    override val span: SourceSpan,
) : Expression
