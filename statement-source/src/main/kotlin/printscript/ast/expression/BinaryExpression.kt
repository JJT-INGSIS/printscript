package printscript.ast.expression

import printscript.model.source.SourceSpan

public data class BinaryExpression(
    public val left: Expression,
    public val operator: BinaryOperator,
    public val operatorSpan: SourceSpan,
    public val right: Expression,
) : Expression {

    override val span: SourceSpan = SourceSpan(
        start = left.span.start,
        end = right.span.end,
    )
}
