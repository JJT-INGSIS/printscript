package printscript.parser.internal.statement

import printscript.ast.Identifier
import printscript.ast.expression.Expression
import printscript.ast.statement.AssignmentStatement
import printscript.ast.statement.Statement
import printscript.model.source.SourceSpan
import printscript.parser.internal.ParsingResult
import printscript.parser.internal.context.ParsingContext
import printscript.parser.internal.expression.ExpressionParser
import printscript.parser.internal.orReturn
import printscript.token.Token
import printscript.token.TokenType

internal class AssignmentParser(
    private val expressionParser: ExpressionParser,
    override val followingTokenType: TokenType = DEFAULT_FOLLOWING_TOKEN,
) : TargetedStatementParser {

    override fun parseStatement(target: Identifier, context: ParsingContext): ParsingResult<Statement> {
        val components = readComponents(context)
            .orReturn { return it }

        return ParsingResult.Success(
            value = buildStatement(target, components.value),
            resultingContext = components.resultingContext,
        )
    }

    private fun readComponents(context: ParsingContext): ParsingResult<AssignmentComponents> {
        val assignment = context.expect(followingTokenType)
            .orReturn { return it }

        val expression = expressionParser.parseExpression(assignment.resultingContext)
            .orReturn { return it }

        val semicolon = expression.resultingContext.expect(TokenType.SEMICOLON)
            .orReturn { return it }

        return ParsingResult.Success(
            value = AssignmentComponents(
                expression = expression.value,
                semicolonToken = semicolon.value,
            ),
            resultingContext = semicolon.resultingContext,
        )
    }

    private fun buildStatement(target: Identifier, components: AssignmentComponents): Statement {
        return AssignmentStatement(
            target = target,
            expression = components.expression,
            span = SourceSpan(
                start = target.span.start,
                end = components.semicolonToken.span.end,
            ),
        )
    }

    private data class AssignmentComponents(
        val expression: Expression,
        val semicolonToken: Token,
    )

    private companion object {
        val DEFAULT_FOLLOWING_TOKEN = TokenType.ASSIGN
    }
}
