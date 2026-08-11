package printscript.parser.internal.expression

import printscript.model.ast.Identifier
import printscript.model.ast.expression.Expression
import printscript.model.ast.expression.GroupingExpression
import printscript.model.ast.expression.IdentifierExpression
import printscript.model.ast.expression.NumberLiteralExpression
import printscript.model.ast.expression.StringLiteralExpression
import printscript.model.ast.expression.StringQuoteStyle
import printscript.model.source.SourceSpan
import printscript.parser.internal.ParsingContext
import printscript.parser.internal.ParsingResult
import printscript.parser.internal.orReturn
import printscript.statement.ParseError
import printscript.token.Token
import printscript.token.TokenType

internal class PrimaryExpressionParser(
    private val parseNestedExpression:
        (ParsingContext) -> ParsingResult<Expression>,
) : ExpressionParser {

    override fun parse(
        context: ParsingContext,
    ): ParsingResult<Expression> {
        val token = context.peek()
            .orReturn { return it }

        return when (token.type) {
            TokenType.NUMBER_LITERAL ->
                consumeAndParse(context, ::numberLiteral)

            TokenType.STRING_LITERAL ->
                consumeAndParse(context, ::stringLiteral)

            TokenType.IDENTIFIER ->
                consumeAndParse(context, ::identifier)

            TokenType.LEFT_PAREN ->
                grouping(context)

            else ->
                unexpectedToken(token)
        }
    }

    private fun consumeAndParse(
        context: ParsingContext,
        parser: (Token) -> ParsingResult<Expression>,
    ): ParsingResult<Expression> {
        val token = context.consume()
            .orReturn { return it }

        return parser(token)
    }

    private fun numberLiteral(
        token: Token,
    ): ParsingResult<Expression> {
        val value = token.lexeme.toBigDecimalOrNull()
            ?: return invalidLiteral(token)

        return ParsingResult.Success(
            NumberLiteralExpression(
                value = value,
                span = token.span,
            ),
        )
    }

    private fun stringLiteral(
        token: Token,
    ): ParsingResult<Expression> {
        if (token.lexeme.length < MINIMUM_QUOTED_LITERAL_LENGTH) {
            return invalidLiteral(token)
        }

        val openingQuote = token.lexeme.first()
        val closingQuote = token.lexeme.last()

        val quoteStyle = QUOTE_STYLES[openingQuote]
            ?: return invalidLiteral(token)

        if (openingQuote != closingQuote) {
            return invalidLiteral(token)
        }

        val value = token.lexeme.substring(
            startIndex = QUOTE_LENGTH,
            endIndex = token.lexeme.length - QUOTE_LENGTH,
        )

        return ParsingResult.Success(
            StringLiteralExpression(
                value = value,
                quoteStyle = quoteStyle,
                span = token.span,
            ),
        )
    }

    private fun identifier(
        token: Token,
    ): ParsingResult<Expression> {
        return ParsingResult.Success(
            IdentifierExpression(
                Identifier(
                    value = token.lexeme,
                    span = token.span,
                ),
            ),
        )
    }

    private fun grouping(
        context: ParsingContext,
    ): ParsingResult<Expression> {
        val openParenthesis = context.expect(TokenType.LEFT_PAREN)
            .orReturn { return it }

        val expression = parseNestedExpression(context)
            .orReturn { return it }

        val closeParenthesis = context.expect(TokenType.RIGHT_PAREN)
            .orReturn { return it }

        return ParsingResult.Success(
            GroupingExpression(
                expression = expression,
                span = SourceSpan(
                    start = openParenthesis.span.start,
                    end = closeParenthesis.span.end,
                ),
            ),
        )
    }

    private fun unexpectedToken(
        token: Token,
    ): ParsingResult.Failure {
        return ParsingResult.Failure(
            ParseError.UnexpectedToken(
                expected = PRIMARY_START_TOKENS,
                actual = token,
            ),
        )
    }

    private fun invalidLiteral(
        token: Token,
    ): ParsingResult.Failure {
        return ParsingResult.Failure(
            ParseError.InvalidLiteral(token),
        )
    }

    private companion object {
        const val QUOTE_LENGTH = 1
        const val MINIMUM_QUOTED_LITERAL_LENGTH = QUOTE_LENGTH * 2

        val QUOTE_STYLES = mapOf(
            '\'' to StringQuoteStyle.SINGLE,
            '"' to StringQuoteStyle.DOUBLE,
        )

        val PRIMARY_START_TOKENS = setOf(
            TokenType.NUMBER_LITERAL,
            TokenType.STRING_LITERAL,
            TokenType.IDENTIFIER,
            TokenType.LEFT_PAREN,
        )
    }
}