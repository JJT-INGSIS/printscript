package printscript.v1.parser.internal.expression

import printscript.ast.expression.BooleanLiteralExpression
import printscript.ast.expression.Expression
import printscript.ast.expression.ReadEnvironmentExpression
import printscript.ast.expression.ReadInputExpression
import printscript.model.source.SourceSpan
import printscript.parser.ParsingContext
import printscript.parser.ParsingResult
import printscript.parser.expression.ExpressionParser
import printscript.parser.expression.PrimaryExpressionParser
import printscript.parser.orReturn
import printscript.token.TokenType
import printscript.v1.token.PrintScriptV1TokenType

internal class PrintScriptV11PrimaryExpressionParser(
    private val v1Parser: PrimaryExpressionParser<Expression>,
    booleanValuesByTokenType: Map<TokenType, Boolean>,
) : PrimaryExpressionParser<Expression> {

    private val booleanValuesByTokenType = booleanValuesByTokenType.toMap()

    override fun parsePrimaryExpression(
        context: ParsingContext,
        nestedExpressionParser: ExpressionParser<Expression>,
    ): ParsingResult<Expression> {
        val peeked = context.peek()
            .orReturn { return it }
        val booleanValue = booleanValuesByTokenType[peeked.value.type]

        if (booleanValue != null) {
            return parseBooleanLiteral(
                context = peeked.resultingContext,
                value = booleanValue,
            )
        }

        return when (peeked.value.type) {
            PrintScriptV1TokenType.READ_INPUT ->
                parseFunctionCall(
                    context = peeked.resultingContext,
                    nestedExpressionParser = nestedExpressionParser,
                    buildExpression = ::ReadInputExpression,
                )

            PrintScriptV1TokenType.READ_ENV ->
                parseFunctionCall(
                    context = peeked.resultingContext,
                    nestedExpressionParser = nestedExpressionParser,
                    buildExpression = ::ReadEnvironmentExpression,
                )

            else ->
                v1Parser.parsePrimaryExpression(
                    context = peeked.resultingContext,
                    nestedExpressionParser = nestedExpressionParser,
                )
        }
    }

    private fun parseBooleanLiteral(context: ParsingContext, value: Boolean): ParsingResult<Expression> {
        val token = context.consume()
            .orReturn { return it }

        return ParsingResult.Success(
            value = BooleanLiteralExpression(
                value = value,
                span = token.value.span,
            ),
            resultingContext = token.resultingContext,
        )
    }

    private fun parseFunctionCall(
        context: ParsingContext,
        nestedExpressionParser: ExpressionParser<Expression>,
        buildExpression: (Expression, SourceSpan) -> Expression,
    ): ParsingResult<Expression> {
        val function = context.consume()
            .orReturn { return it }
        val opening = function.resultingContext.expect(PrintScriptV1TokenType.LEFT_PAREN)
            .orReturn { return it }
        val argument = nestedExpressionParser.parseExpression(opening.resultingContext)
            .orReturn { return it }
        val closing = argument.resultingContext.expect(PrintScriptV1TokenType.RIGHT_PAREN)
            .orReturn { return it }

        return ParsingResult.Success(
            value = buildExpression(
                argument.value,
                SourceSpan(
                    start = function.value.span.start,
                    end = closing.value.span.end,
                ),
            ),
            resultingContext = closing.resultingContext,
        )
    }
}
