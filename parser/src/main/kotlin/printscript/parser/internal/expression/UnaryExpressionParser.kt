package printscript.parser.internal.expression

import printscript.parser.ParsingContext
import printscript.parser.ParsingResult
import printscript.parser.expression.ExpressionParser
import printscript.parser.expression.UnaryExpressionBuilder
import printscript.parser.orReturn
import printscript.token.TokenType

internal class UnaryExpressionParser<E>(
    private val operandParser: ExpressionParser<E>,
    private val expressionBuilders: Map<TokenType, UnaryExpressionBuilder<E>>,
) : ExpressionParser<E> {

    override fun parseExpression(context: ParsingContext): ParsingResult<E> {
        val peeked = context.peek()
            .orReturn { return it }

        val expressionBuilder = expressionBuilders[peeked.value.type]
            ?: return operandParser.parseExpression(peeked.resultingContext)

        return parseUnaryExpression(
            expressionBuilder = expressionBuilder,
            context = peeked.resultingContext,
        )
    }

    private fun parseUnaryExpression(
        expressionBuilder: UnaryExpressionBuilder<E>,
        context: ParsingContext,
    ): ParsingResult<E> {
        val operatorToken = context.consume()
            .orReturn { return it }

        val operand = parseExpression(operatorToken.resultingContext)
            .orReturn { return it }

        return ParsingResult.Success(
            value = expressionBuilder.build(
                operatorToken = operatorToken.value,
                operand = operand.value,
            ),
            resultingContext = operand.resultingContext,
        )
    }
}
