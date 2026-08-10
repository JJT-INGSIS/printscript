package printscript.parser.internal.expression

import printscript.model.ast.expression.Expression
import printscript.parser.internal.ParsingContext
import printscript.parser.internal.ParsingResult

internal interface ExpressionParser {
    fun parse(
        context: ParsingContext,
    ): ParsingResult<Expression>
}