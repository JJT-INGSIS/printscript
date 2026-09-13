package printscript.v1.parser.internal.expression

import printscript.ast.expression.BooleanLiteralExpression
import printscript.ast.expression.Expression
import printscript.parser.ParsingContext
import printscript.parser.ParsingResult
import printscript.parser.expression.ExpressionParser
import printscript.parser.orReturn
import printscript.token.TokenType

internal class BooleanLiteralRule(
    booleanValuesByTokenType: Map<TokenType, Boolean>,
) : PrimaryExpressionRule {

    private val booleanValuesByTokenType = booleanValuesByTokenType.toMap()

    override val startTokenTypes: Set<TokenType> = this.booleanValuesByTokenType.keys

    override fun parse(
        context: ParsingContext,
        nestedExpressionParser: ExpressionParser<Expression>,
    ): ParsingResult<Expression> {
        val token = context.consume()
            .orReturn { return it }

        val value = requireNotNull(booleanValuesByTokenType[token.value.type])

        return ParsingResult.Success(
            value = BooleanLiteralExpression(
                value = value,
                span = token.value.span,
            ),
            resultingContext = token.resultingContext,
        )
    }
}
