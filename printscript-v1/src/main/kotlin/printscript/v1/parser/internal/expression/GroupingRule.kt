package printscript.v1.parser.internal.expression

import printscript.ast.expression.Expression
import printscript.ast.expression.GroupingExpression
import printscript.model.source.SourceSpan
import printscript.parser.ParsingContext
import printscript.parser.ParsingResult
import printscript.parser.expression.ExpressionParser
import printscript.parser.orReturn
import printscript.token.TokenType
import printscript.v1.token.PrintScriptV1TokenType

internal class GroupingRule : PrimaryExpressionRule {

    override val startTokenTypes: Set<TokenType> = setOf(PrintScriptV1TokenType.LEFT_PAREN)

    override fun parse(
        context: ParsingContext,
        nestedExpressionParser: ExpressionParser<Expression>,
    ): ParsingResult<Expression> {
        val openParenthesis = context.consume()
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
}
