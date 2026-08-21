package printscript.parser.internal.expression

import printscript.ast.expression.Expression
import printscript.ast.expression.UnaryExpression
import printscript.ast.expression.UnaryOperator
import printscript.parser.internal.ParsingResult
import printscript.parser.internal.context.ParsingContext
import printscript.parser.internal.orReturn
import printscript.token.TokenType

internal class UnaryExpressionParser(
    private val operandParser: ExpressionParser,
    private val operators: Map<TokenType, UnaryOperator>,
) : ExpressionParser {

    override fun parseExpression(
        context: ParsingContext,
    ): ParsingResult<Expression> {
        val peeked = context.peek()
            .orReturn { return it }

        val operator = operators[peeked.value.type]
            ?: return operandParser.parseExpression(peeked.resultingContext)

        return parseUnaryExpression(
            operator = operator,
            context = peeked.resultingContext,
        )
    }

    private fun parseUnaryExpression(
        operator: UnaryOperator,
        context: ParsingContext,
    ): ParsingResult<Expression> {
        val operatorToken = context.consume()
            .orReturn { return it }

        val operand = parseExpression(operatorToken.resultingContext)
            .orReturn { return it }

        return ParsingResult.Success(
            value = UnaryExpression(
                operator = operator,
                operatorSpan = operatorToken.value.span,
                operand = operand.value,
            ),
            resultingContext = operand.resultingContext,
        )
    }
}
