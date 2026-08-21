package printscript.parser.internal.expression

import printscript.ast.expression.BinaryExpression
import printscript.ast.expression.BinaryOperator
import printscript.ast.expression.Expression
import printscript.parser.internal.ParsingResult
import printscript.parser.internal.context.ParsingContext
import printscript.parser.internal.orReturn
import printscript.token.Token
import printscript.token.TokenType

internal class LeftAssociativeBinaryExpressionParser(
    private val operandParser: ExpressionParser,
    private val operators: Map<TokenType, BinaryOperator>,
) : ExpressionParser {

    override fun parseExpression(
        context: ParsingContext,
    ): ParsingResult<Expression> {
        val firstOperand = operandParser.parseExpression(context)
            .orReturn { return it }

        return parseRemainingOperands(firstOperand)
    }

    /**
     * Acumula hacia la izquierda: cada operando nuevo envuelve a lo ya
     * parseado, que es lo que hace al operador asociativo a izquierda.
     */
    private tailrec fun parseRemainingOperands(
        left: ParsingResult.Success<Expression>,
    ): ParsingResult<Expression> {
        val peeked = left.resultingContext.peek()
            .orReturn { return it }

        val operator = operators[peeked.value.type]
            ?: return left.copy(resultingContext = peeked.resultingContext)

        val operatorToken = peeked.resultingContext.consume()
            .orReturn { return it }

        val right = operandParser.parseExpression(operatorToken.resultingContext)
            .orReturn { return it }

        return parseRemainingOperands(
            left = combine(
                left = left,
                operator = operator,
                operatorToken = operatorToken,
                right = right,
            ),
        )
    }

    private fun combine(
        left: ParsingResult.Success<Expression>,
        operator: BinaryOperator,
        operatorToken: ParsingResult.Success<Token>,
        right: ParsingResult.Success<Expression>,
    ): ParsingResult.Success<Expression> {
        return ParsingResult.Success(
            value = BinaryExpression(
                left = left.value,
                operator = operator,
                operatorSpan = operatorToken.value.span,
                right = right.value,
            ),
            resultingContext = right.resultingContext,
        )
    }
}
