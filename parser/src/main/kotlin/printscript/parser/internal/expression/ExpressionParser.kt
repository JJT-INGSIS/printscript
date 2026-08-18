package printscript.parser.internal.expression

import printscript.ast.expression.Expression
import printscript.parser.internal.context.ParsingContext
import printscript.parser.internal.ParsingResult

internal interface ExpressionParser {
    fun parseExpression(
        context: ParsingContext,
    ): ParsingResult<Expression>
}