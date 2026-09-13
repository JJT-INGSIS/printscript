package printscript.v1.parser.internal.expression

import printscript.ast.expression.Expression
import printscript.model.source.SourceSpan
import printscript.parser.ParsingContext
import printscript.parser.ParsingResult
import printscript.parser.expression.ExpressionParser
import printscript.parser.orReturn
import printscript.token.TokenType
import printscript.v1.token.PrintScriptV1TokenType

internal class SingleArgumentFunctionCallRule(
    functionTokenType: TokenType,
    private val buildExpression: (Expression, SourceSpan) -> Expression,
) : PrimaryExpressionRule {

    override val startTokenTypes: Set<TokenType> = setOf(functionTokenType)

    override fun parse(
        context: ParsingContext,
        nestedExpressionParser: ExpressionParser<Expression>,
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
