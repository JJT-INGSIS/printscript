package printscript.parser.internal.expression

import printscript.model.ast.expression.Expression
import printscript.model.ast.expression.UnaryExpression
import printscript.model.ast.expression.UnaryOperator
import printscript.parser.internal.context.ParsingContext
import printscript.parser.internal.ParsingResult
import printscript.parser.internal.orReturn
import printscript.token.TokenType

internal class UnaryExpressionParser(
    private val operandParser: ExpressionParser,
    private val operators: Map<TokenType, UnaryOperator>,
) : ExpressionParser {

    override fun parse(
        context: ParsingContext,
    ): ParsingResult<Expression> {
        val nextToken = context.peek()
            .orReturn { return it }

        val operator = operators[nextToken.type]
            ?: return operandParser.parse(context)

        val operatorToken = context.consume()
            .orReturn { return it }

        val operand = parse(context)
            .orReturn { return it }

        return ParsingResult.Success(
            UnaryExpression(
                operator = operator,
                operatorSpan = operatorToken.span,
                operand = operand,
            ),
        )
    }
}