package printscript.v1.parser.internal.expression

import printscript.ast.Identifier
import printscript.ast.expression.Expression
import printscript.ast.expression.IdentifierExpression
import printscript.parser.ParsingContext
import printscript.parser.ParsingResult
import printscript.parser.expression.ExpressionParser
import printscript.parser.orReturn
import printscript.token.TokenType
import printscript.v1.token.PrintScriptV1TokenType

internal class IdentifierRule : PrimaryExpressionRule {

    override val startTokenTypes: Set<TokenType> = setOf(PrintScriptV1TokenType.IDENTIFIER)

    override fun parse(
        context: ParsingContext,
        nestedExpressionParser: ExpressionParser<Expression>,
    ): ParsingResult<Expression> {
        val token = context.consume()
            .orReturn { return it }

        return ParsingResult.Success(
            value = IdentifierExpression(
                Identifier(
                    value = token.value.lexeme,
                    span = token.value.span,
                ),
            ),
            resultingContext = token.resultingContext,
        )
    }
}
