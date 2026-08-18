package printscript.parser.internal.statement

import printscript.model.ast.Identifier
import printscript.model.ast.expression.Expression
import printscript.model.ast.statement.AssignmentStatement
import printscript.model.ast.statement.Statement
import printscript.model.source.SourceSpan
import printscript.parser.internal.context.ParsingContext
import printscript.parser.internal.ParsingResult
import printscript.parser.internal.context.TokenLookahead
import printscript.parser.internal.expression.ExpressionParser
import printscript.parser.internal.orReturn
import printscript.token.Token
import printscript.token.TokenType

internal class AssignmentParser(
    private val expressionParser: ExpressionParser,
    private val targetTokens: Set<TokenType> = DEFAULT_TARGET_TOKENS,
    private val assignmentTokens: Set<TokenType> = DEFAULT_ASSIGNMENT_TOKENS,
) : StatementParser {

    private val matcher = StatementMatcher(
        listOf(targetTokens, assignmentTokens),
    )

    override fun matchInitialTokens(
        lookahead: TokenLookahead,
    ): StatementMatch {
        return matcher.matchInitialTokens(lookahead)
    }

    override fun parseStatement(
        context: ParsingContext,
    ): ParsingResult<Statement> {
        val components = readComponents(context)
            .orReturn { return it }

        return ParsingResult.Success(buildStatement(components))
    }

    private fun readComponents(
        context: ParsingContext,
    ): ParsingResult<AssignmentComponents> {
        val targetToken = context.expect(targetTokens)
            .orReturn { return it }

        context.expect(assignmentTokens)
            .orReturn { return it }

        val expression = expressionParser.parseExpression(context)
            .orReturn { return it }

        val semicolonToken = context.expect(TokenType.SEMICOLON)
            .orReturn { return it }

        return ParsingResult.Success(
            AssignmentComponents(
                targetToken = targetToken,
                expression = expression,
                semicolonToken = semicolonToken,
            ),
        )
    }

    private fun buildStatement(
        components: AssignmentComponents,
    ): Statement {
        return AssignmentStatement(
            target = Identifier(
                value = components.targetToken.lexeme,
                span = components.targetToken.span,
            ),
            expression = components.expression,
            span = SourceSpan(
                start = components.targetToken.span.start,
                end = components.semicolonToken.span.end,
            ),
        )
    }

    private data class AssignmentComponents(
        val targetToken: Token,
        val expression: Expression,
        val semicolonToken: Token,
    )

    private companion object {
        val DEFAULT_TARGET_TOKENS = setOf(
            TokenType.IDENTIFIER,
        )

        val DEFAULT_ASSIGNMENT_TOKENS = setOf(
            TokenType.ASSIGN,
        )
    }
}