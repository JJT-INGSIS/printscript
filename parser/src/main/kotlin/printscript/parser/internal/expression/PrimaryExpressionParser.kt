package printscript.parser.internal.expression

import printscript.ast.Identifier
import printscript.ast.expression.Expression
import printscript.ast.expression.GroupingExpression
import printscript.ast.expression.IdentifierExpression
import printscript.ast.expression.NumberLiteralExpression
import printscript.ast.expression.StringLiteralExpression
import printscript.ast.expression.StringQuoteStyle
import printscript.model.source.SourceSpan
import printscript.parser.internal.context.ParsingContext
import printscript.parser.internal.ParsingResult
import printscript.parser.internal.orReturn
import printscript.statement.ParseError
import printscript.token.Token
import printscript.token.TokenType

internal class PrimaryExpressionParser(
    private val parseNestedExpression:
        (ParsingContext) -> ParsingResult<Expression>,
) : ExpressionParser {

    override fun parseExpression(
        context: ParsingContext,
    ): ParsingResult<Expression> {
        val token = context.peek()
            .orReturn { return it }

        return when (token.type) {
            TokenType.NUMBER_LITERAL ->
                consumeAndBuild(context, ::numberLiteral)

            TokenType.STRING_LITERAL ->
                consumeAndBuild(context, ::stringLiteral)

            TokenType.IDENTIFIER ->
                consumeAndBuild(context, ::identifier)

            TokenType.LEFT_PAREN ->
                parseGrouping(context)

            else ->
                unexpectedToken(token)
        }
    }

    private fun consumeAndBuild(
        context: ParsingContext,
        build: (Token) -> ParsingResult<Expression>,
    ): ParsingResult<Expression> {
        val token = context.consume()
            .orReturn { return it }

        return build(token)
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

        val quoteStyle = QUOTE_STYLE_BY_DELIMITER[openingQuote]
            ?: return invalidLiteral(token)

        if (openingQuote != closingQuote) {
            return invalidLiteral(token)
        }

        return ParsingResult.Success(
            StringLiteralExpression(
                value = unquote(token.lexeme),
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

    private fun parseGrouping(
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

    private fun unquote(
        lexeme: String,
    ): String {
        return lexeme.substring(
            startIndex = QUOTE_LENGTH,
            endIndex = lexeme.length - QUOTE_LENGTH,
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

        val QUOTE_STYLE_BY_DELIMITER = mapOf(
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