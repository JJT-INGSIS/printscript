package printscript.parser.internal.expression

import printscript.ast.expression.Expression
import printscript.parser.internal.ParsingResult
import printscript.parser.internal.context.ParsingContext

internal interface ExpressionParser {
    fun parseExpression(context: ParsingContext): ParsingResult<Expression>
}
