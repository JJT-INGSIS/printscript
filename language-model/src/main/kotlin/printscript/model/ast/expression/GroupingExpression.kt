package printscript.model.ast.expression

import printscript.model.source.SourceSpan

data class GroupingExpression(
    val expression: Expression,
    override val span: SourceSpan,
) : Expression