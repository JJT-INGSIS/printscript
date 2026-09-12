package printscript.v1.parser.internal.expression

import printscript.ast.expression.Expression
import printscript.ast.expression.NumberLiteralExpression
import printscript.parser.ParsingContext
import printscript.parser.ParsingResult
import printscript.parser.expression.ExpressionParser
import printscript.parser.orReturn
import printscript.statement.ParseError
import printscript.token.Token
import printscript.token.TokenType
import printscript.v1.token.PrintScriptV1TokenType

internal class NumberLiteralRule : PrimaryExpressionRule {

    override val startTokenTypes: Set<TokenType> = setOf(PrintScriptV1TokenType.NUMBER_LITERAL)

    override fun parse(
        context: ParsingContext,
        nestedExpressionParser: ExpressionParser<Expression>,
    ): ParsingResult<Expression> {
        val token = context.consume()
            .orReturn { return it }

        val value = token.value.lexeme.toBigDecimalOrNull()
            ?: return invalidLiteral(token.value)

        return ParsingResult.Success(
            value = NumberLiteralExpression(
                value = value,
                span = token.value.span,
            ),
            resultingContext = token.resultingContext,
        )
    }

    private fun invalidLiteral(token: Token): ParsingResult.Failure {
        return ParsingResult.Failure(
            ParseError.InvalidLiteral(token),
        )
    }
}
