package printscript.ast.expression

import printscript.model.source.SourceSpan

public data class UnaryExpression(
    public val operator: UnaryOperator,
    public val operatorSpan: SourceSpan,
    public val operand: Expression,
) : Expression {

    override val span: SourceSpan = SourceSpan(
        start = operatorSpan.start,
        end = operand.span.end,
    )
}
