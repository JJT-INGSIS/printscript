package printscript.v1.parser.internal.expression

import printscript.ast.expression.Expression
import printscript.ast.expression.StringLiteralExpression
import printscript.ast.expression.StringQuoteStyle
import printscript.parser.ParsingContext
import printscript.parser.ParsingResult
import printscript.parser.expression.ExpressionParser
import printscript.parser.orReturn
import printscript.statement.ParseError
import printscript.token.Token
import printscript.token.TokenType
import printscript.v1.token.PrintScriptV1TokenType

private const val QUOTE_LENGTH = 1
private const val MINIMUM_QUOTED_LITERAL_LENGTH = QUOTE_LENGTH * 2

internal class StringLiteralRule(
    quoteStyleByDelimiter: Map<Char, StringQuoteStyle>,
) : PrimaryExpressionRule {

    private val quoteStyleByDelimiter = quoteStyleByDelimiter.toMap()

    override val startTokenTypes: Set<TokenType> = setOf(PrintScriptV1TokenType.STRING_LITERAL)

    override fun parse(
        context: ParsingContext,
        nestedExpressionParser: ExpressionParser<Expression>,
    ): ParsingResult<Expression> {
        val token = context.consume()
            .orReturn { return it }

        return stringLiteral(token.value, token.resultingContext)
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

    private fun unquote(lexeme: String): String {
        return lexeme.substring(
            startIndex = QUOTE_LENGTH,
            endIndex = lexeme.length - QUOTE_LENGTH,
        )
    }

    private fun invalidLiteral(token: Token): ParsingResult.Failure {
        return ParsingResult.Failure(
            ParseError.InvalidLiteral(token),
        )
    }
}
