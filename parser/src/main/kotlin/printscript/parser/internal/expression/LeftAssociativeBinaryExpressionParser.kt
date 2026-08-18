package printscript.parser.internal.expression

import printscript.ast.expression.BinaryExpression
import printscript.ast.expression.Expression
import printscript.ast.expression.BinaryOperator
import printscript.parser.internal.context.ParsingContext
import printscript.parser.internal.ParsingResult
import printscript.parser.internal.orReturn
import printscript.token.TokenType


internal class LeftAssociativeBinaryExpressionParser(
    private val operandParser: ExpressionParser,
    private val operators: Map<TokenType, BinaryOperator>,
) : ExpressionParser {

    override fun parseExpression(
        context: ParsingContext,
    ): ParsingResult<Expression> {
        var left = operandParser.parseExpression(context)
            .orReturn { return it }

        while (true) {
            val nextToken = context.peek()
                .orReturn { return it }

            val operator = operators[nextToken.type]
                ?: break

            val operatorToken = context.consume()
                .orReturn { return it }

            val right = operandParser.parseExpression(context)
                .orReturn { return it }

            left = BinaryExpression(
                left = left,
                operator = operator,
                operatorSpan = operatorToken.span,
                right = right,
            )
        }

        return ParsingResult.Success(left)
    }
}