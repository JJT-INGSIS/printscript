package printscript.parser.internal.statement

import printscript.model.ast.Identifier
import printscript.model.ast.expression.Expression
import printscript.model.ast.statement.AssignmentStatement
import printscript.model.ast.statement.Statement
import printscript.model.source.SourceSpan
import printscript.parser.internal.ParsingContext
import printscript.parser.internal.ParsingResult
import printscript.parser.internal.expression.ExpressionParser
import printscript.token.Token
import printscript.token.TokenType

internal class AssignmentParser(
    private val expressionParser: ExpressionParser,
) : StatementParser {

    override fun canStartWith(type: TokenType): Boolean = type == TokenType.IDENTIFIER

    override fun parse(context: ParsingContext): ParsingResult<Statement> {
        val parts = checkGrammar(context).orReturn { return it }
        return ParsingResult.Success(build(parts))
    }

    private fun checkGrammar(context: ParsingContext): ParsingResult<Parts> {
        val identifierToken = context.expect(TokenType.IDENTIFIER).orReturn { return it }
        context.expect(TokenType.ASSIGN).orReturn { return it }
        val expression = expressionParser.parse(context).orReturn { return it }
        val semicolonToken = context.expect(TokenType.SEMICOLON).orReturn { return it }
        return ParsingResult.Success(
            Parts(identifierToken, expression, semicolonToken),
        )
    }

    private fun build(parts: Parts): Statement =
        AssignmentStatement(
            target = Identifier(
                value = parts.identifierToken.lexeme,
                span = parts.identifierToken.span,
            ),
            expression = parts.expression,
            span = SourceSpan(
                start = parts.identifierToken.span.start,
                end = parts.semicolonToken.span.end,
            ),
        )

    private inline fun <T> ParsingResult<T>.orReturn(
        onFailure: (ParsingResult.Failure) -> Nothing,
    ): T =
        when (this) {
            is ParsingResult.Success -> value
            is ParsingResult.Failure -> onFailure(this)
        }

    private data class Parts(
        val identifierToken: Token,
        val expression: Expression,
        val semicolonToken: Token,
    )
}
