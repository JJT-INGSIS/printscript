package printscript.v1.parser.internal.statement

import printscript.ast.Identifier
import printscript.ast.expression.Expression
import printscript.ast.statement.AssignmentStatement
import printscript.ast.statement.Statement
import printscript.model.source.SourceSpan
import printscript.parser.ParsingContext
import printscript.parser.ParsingResult
import printscript.parser.expression.ExpressionParser
import printscript.parser.orReturn
import printscript.token.Token
import printscript.token.TokenType

internal class AssignmentParser(
    private val expressionParser: ExpressionParser<Expression>,
    override val followingTokenType: TokenType,
    private val statementTerminator: StatementTerminator,
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

        val terminator = statementTerminator.consume(expression.resultingContext)
            .orReturn { return it }

        return ParsingResult.Success(
            value = AssignmentComponents(
                expression = expression.value,
                terminatorToken = terminator.value,
            ),
            resultingContext = terminator.resultingContext,
        )
    }

    private fun buildStatement(target: Identifier, components: AssignmentComponents): Statement {
        return AssignmentStatement(
            target = target,
            expression = components.expression,
            span = SourceSpan(
                start = target.span.start,
                end = components.terminatorToken.span.end,
            ),
        )
    }

    private data class AssignmentComponents(
        val expression: Expression,
        val terminatorToken: Token,
    )
}
