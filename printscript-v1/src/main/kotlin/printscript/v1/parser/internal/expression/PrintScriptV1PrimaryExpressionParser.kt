package printscript.v1.parser.internal.expression

import printscript.ast.Identifier
import printscript.ast.expression.Expression
import printscript.ast.expression.GroupingExpression
import printscript.ast.expression.IdentifierExpression
import printscript.ast.expression.NumberLiteralExpression
import printscript.ast.expression.StringLiteralExpression
import printscript.ast.expression.StringQuoteStyle
import printscript.model.source.SourceSpan
import printscript.parser.ParsingContext
import printscript.parser.ParsingResult
import printscript.parser.expression.ExpressionParser
import printscript.parser.expression.PrimaryExpressionParser
import printscript.parser.orReturn
import printscript.statement.ParseError
import printscript.token.Token
import printscript.token.TokenType
import printscript.v1.token.PrintScriptV1TokenType

internal class PrintScriptV1PrimaryExpressionParser(
    quoteStyleByDelimiter: Map<Char, StringQuoteStyle>,
) : PrimaryExpressionParser<Expression> {

    private val quoteStyleByDelimiter = quoteStyleByDelimiter.toMap()

    override fun parsePrimaryExpression(
        context: ParsingContext,
        nestedExpressionParser: ExpressionParser<Expression>,
    ): ParsingResult<Expression> {
        val peeked = context.peek()
            .orReturn { return it }

        return when (peeked.value.type) {
            PrintScriptV1TokenType.NUMBER_LITERAL ->
                consumeAndBuild(peeked.resultingContext, ::numberLiteral)

            PrintScriptV1TokenType.STRING_LITERAL ->
                consumeAndBuild(peeked.resultingContext, ::stringLiteral)

            PrintScriptV1TokenType.IDENTIFIER ->
                consumeAndBuild(peeked.resultingContext, ::identifier)

            PrintScriptV1TokenType.LEFT_PAREN ->
                parseGrouping(
                    context = peeked.resultingContext,
                    nestedExpressionParser = nestedExpressionParser,
                )

            else -> unexpectedToken(peeked.value)
        }
    }

    private fun consumeAndBuild(
        context: ParsingContext,
        build: (Token, ParsingContext) -> ParsingResult<Expression>,
    ): ParsingResult<Expression> {
        val token = context.consume()
            .orReturn { return it }

        return build(token.value, token.resultingContext)
    }

    private fun numberLiteral(token: Token, context: ParsingContext): ParsingResult<Expression> {
        val value = token.lexeme.toBigDecimalOrNull()
            ?: return invalidLiteral(token)

        return ParsingResult.Success(
            value = NumberLiteralExpression(
                value = value,
                span = token.span,
            ),
            resultingContext = context,
        )
    }

    private fun stringLiteral(token: Token, context: ParsingContext): ParsingResult<Expression> {
        if (token.lexeme.length < MINIMUM_QUOTED_LITERAL_LENGTH) {
            return invalidLiteral(token)
        }

        val openingQuote = token.lexeme.first()
        val closingQuote = token.lexeme.last()
        val quoteStyle = quoteStyleByDelimiter[openingQuote]
            ?: return invalidLiteral(token)

        if (openingQuote != closingQuote) {
            return invalidLiteral(token)
        }

        return ParsingResult.Success(
            value = StringLiteralExpression(
                value = unquote(token.lexeme),
                quoteStyle = quoteStyle,
                span = token.span,
            ),
            resultingContext = context,
        )
    }

    private fun identifier(token: Token, context: ParsingContext): ParsingResult<Expression> {
        return ParsingResult.Success(
            value = IdentifierExpression(
                Identifier(
                    value = token.lexeme,
                    span = token.span,
                ),
            ),
            resultingContext = context,
        )
    }

    private fun parseGrouping(
        context: ParsingContext,
        nestedExpressionParser: ExpressionParser<Expression>,
    ): ParsingResult<Expression> {
        val openParenthesis = context.expect(PrintScriptV1TokenType.LEFT_PAREN)
            .orReturn { return it }

        val expression = nestedExpressionParser.parseExpression(openParenthesis.resultingContext)
            .orReturn { return it }

        val closeParenthesis = expression.resultingContext.expect(PrintScriptV1TokenType.RIGHT_PAREN)
            .orReturn { return it }

        return ParsingResult.Success(
            value = GroupingExpression(
                expression = expression.value,
                span = SourceSpan(
                    start = openParenthesis.value.span.start,
                    end = closeParenthesis.value.span.end,
                ),
            ),
            resultingContext = closeParenthesis.resultingContext,
        )
    }

    private fun unquote(lexeme: String): String {
        return lexeme.substring(
            startIndex = QUOTE_LENGTH,
            endIndex = lexeme.length - QUOTE_LENGTH,
        )
    }

    private fun unexpectedToken(token: Token): ParsingResult.Failure {
        return ParsingResult.Failure(
            ParseError.UnexpectedToken(
                expected = PRIMARY_START_TOKENS,
                actual = token,
            ),
        )
    }

    private fun invalidLiteral(token: Token): ParsingResult.Failure {
        return ParsingResult.Failure(
            ParseError.InvalidLiteral(token),
        )
    }

    private companion object {
        const val QUOTE_LENGTH = 1
        const val MINIMUM_QUOTED_LITERAL_LENGTH = QUOTE_LENGTH * 2

        val PRIMARY_START_TOKENS: Set<TokenType> = setOf(
            PrintScriptV1TokenType.NUMBER_LITERAL,
            PrintScriptV1TokenType.STRING_LITERAL,
            PrintScriptV1TokenType.IDENTIFIER,
            PrintScriptV1TokenType.LEFT_PAREN,
        )
    }
}
