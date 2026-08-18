package printscript.ast.expression

import printscript.model.source.SourceSpan

data class BinaryExpression(
    val left: Expression,
    val operator: BinaryOperator,
    val operatorSpan: SourceSpan,
    val right: Expression,
) : Expression {

    override val span: SourceSpan = SourceSpan(
        start = left.span.start,
        end = right.span.end,
    )
}