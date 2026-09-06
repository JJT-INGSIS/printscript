package printscript.v1.parser.internal.statement

import printscript.ast.Identifier
import printscript.ast.expression.Expression
import printscript.ast.statement.AssignmentStatement
import printscript.model.source.SourceSpan
import printscript.parser.ParsingContext
import printscript.parser.ParsingResult
import printscript.parser.StatementParser
import printscript.parser.expression.ExpressionParser
import printscript.parser.orReturn
import printscript.statement.Statement
import printscript.token.Token
import printscript.token.TokenType

internal class AssignmentParser(
    private val expressionParser: ExpressionParser<Expression>,
    override val startTokenType: TokenType,
    private val assignmentTokenType: TokenType,
    private val statementTerminatorTokenType: TokenType,
) : StatementParser {

    override fun parseStatement(context: ParsingContext): ParsingResult<Statement> {
        val components = readComponents(context)
            .orReturn { return it }

        return ParsingResult.Success(
            value = buildStatement(components.value),
            resultingContext = components.resultingContext,
        )
    }

    private fun readComponents(context: ParsingContext): ParsingResult<AssignmentComponents> {
        val target = context.expect(startTokenType)
            .orReturn { return it }

        val assignment = target.resultingContext.expect(assignmentTokenType)
            .orReturn { return it }

        val expression = expressionParser.parseExpression(assignment.resultingContext)
            .orReturn { return it }

        val terminator = expression.resultingContext.expect(statementTerminatorTokenType)
            .orReturn { return it }

        return ParsingResult.Success(
            value = AssignmentComponents(
                targetToken = target.value,
                expression = expression.value,
                terminatorToken = terminator.value,
            ),
            resultingContext = terminator.resultingContext,
        )
    }

    private fun buildStatement(components: AssignmentComponents): Statement {
        val target = Identifier(
            value = components.targetToken.lexeme,
            span = components.targetToken.span,
        )

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
        val targetToken: Token,
        val expression: Expression,
        val terminatorToken: Token,
    )
}
