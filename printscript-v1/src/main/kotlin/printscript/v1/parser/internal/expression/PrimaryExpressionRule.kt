package printscript.v1.parser.internal.expression

import printscript.ast.expression.Expression
import printscript.parser.ParsingContext
import printscript.parser.ParsingResult
import printscript.parser.expression.ExpressionParser
import printscript.token.TokenType

internal interface PrimaryExpressionRule {

    val startTokenTypes: Set<TokenType>

    fun parse(context: ParsingContext, nestedExpressionParser: ExpressionParser<Expression>): ParsingResult<Expression>
}
