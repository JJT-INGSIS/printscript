package printscript.model.ast.expression

import printscript.model.source.SourceSpan

data class UnaryExpression(
    val operator: UnaryOperator,
    val operatorSpan: SourceSpan,
    val operand: Expression,
) : Expression {

    override val span: SourceSpan = SourceSpan(
        start = operatorSpan.start,
        end = operand.span.end,
    )
}
