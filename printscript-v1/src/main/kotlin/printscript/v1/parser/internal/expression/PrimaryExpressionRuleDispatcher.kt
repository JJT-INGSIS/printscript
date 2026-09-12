package printscript.v1.parser.internal.expression

import printscript.ast.expression.Expression
import printscript.parser.ParsingContext
import printscript.parser.ParsingResult
import printscript.parser.expression.ExpressionParser
import printscript.parser.expression.PrimaryExpressionParser
import printscript.parser.orReturn
import printscript.statement.ParseError
import printscript.token.Token
import printscript.token.TokenType

internal class PrimaryExpressionRuleDispatcher(
    rules: List<PrimaryExpressionRule>,
) : PrimaryExpressionParser<Expression> {

    private val rules: List<PrimaryExpressionRule> = rules.toList()

    private val startTokenTypes: Set<TokenType> =
        this.rules.flatMap { rule -> rule.startTokenTypes }.toSet()

    override fun parsePrimaryExpression(
        context: ParsingContext,
        nestedExpressionParser: ExpressionParser<Expression>,
    ): ParsingResult<Expression> {
        val peeked = context.peek()
            .orReturn { return it }

        val rule = rules.firstOrNull { candidate -> peeked.value.type in candidate.startTokenTypes }
            ?: return unexpectedToken(peeked.value)

        return rule.parse(
            context = peeked.resultingContext,
            nestedExpressionParser = nestedExpressionParser,
        )
    }

    private fun unexpectedToken(token: Token): ParsingResult.Failure {
        return ParsingResult.Failure(
            ParseError.UnexpectedToken(
                expected = startTokenTypes,
                actual = token,
            ),
        )
    }
}
