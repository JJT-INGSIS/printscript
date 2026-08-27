package printscript.parser.internal.expression

import printscript.parser.ParsingContext
import printscript.parser.ParsingResult
import printscript.parser.expression.BinaryExpressionBuilder
import printscript.parser.expression.ExpressionParser
import printscript.parser.orReturn
import printscript.token.Token
import printscript.token.TokenType

internal class LeftAssociativeBinaryExpressionParser<E>(
    private val operandParser: ExpressionParser<E>,
    private val expressionBuilders: Map<TokenType, BinaryExpressionBuilder<E>>,
) : ExpressionParser<E> {

    override fun parseExpression(context: ParsingContext): ParsingResult<E> {
        val firstOperand = operandParser.parseExpression(context)
            .orReturn { return it }

        return parseRemainingOperands(firstOperand)
    }

    private tailrec fun parseRemainingOperands(left: ParsingResult.Success<E>): ParsingResult<E> {
        val peeked = left.resultingContext.peek()
            .orReturn { return it }

        val expressionBuilder = expressionBuilders[peeked.value.type]
            ?: return left.copy(resultingContext = peeked.resultingContext)

        val operatorToken = peeked.resultingContext.consume()
            .orReturn { return it }

        val right = operandParser.parseExpression(operatorToken.resultingContext)
            .orReturn { return it }

        return parseRemainingOperands(
            left = combine(
                left = left,
                expressionBuilder = expressionBuilder,
                operatorToken = operatorToken,
                right = right,
            ),
        )
    }

    private fun combine(
        left: ParsingResult.Success<E>,
        expressionBuilder: BinaryExpressionBuilder<E>,
        operatorToken: ParsingResult.Success<Token>,
        right: ParsingResult.Success<E>,
    ): ParsingResult.Success<E> {
        return ParsingResult.Success(
            value = expressionBuilder.build(
                left = left.value,
                operatorToken = operatorToken.value,
                right = right.value,
            ),
            resultingContext = right.resultingContext,
        )
    }
}
