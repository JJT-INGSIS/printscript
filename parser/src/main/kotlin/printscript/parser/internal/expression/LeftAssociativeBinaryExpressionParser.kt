package printscript.parser.internal.expression

import printscript.model.ast.expression.BinaryExpression
import printscript.model.ast.expression.BinaryOperator
import printscript.model.ast.expression.Expression
import printscript.parser.internal.context.ParsingContext
import printscript.parser.internal.ParsingResult
import printscript.parser.internal.orReturn
import printscript.token.TokenType

internal class LeftAssociativeBinaryExpressionParser(
    private val operandParser: ExpressionParser,
    private val operators: Map<TokenType, BinaryOperator>,
) : ExpressionParser {

    override fun parse(
        context: ParsingContext,
    ): ParsingResult<Expression> {
        var left = operandParser.parse(context)
            .orReturn { return it }

        while (true) {
            val nextToken = context.peek()
                .orReturn { return it }

            val operator = operators[nextToken.type]
                ?: break

            val operatorToken = context.consume()
                .orReturn { return it }

            val right = operandParser.parse(context)
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