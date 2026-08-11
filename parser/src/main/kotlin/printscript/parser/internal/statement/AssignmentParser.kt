package printscript.parser.internal.statement

import printscript.model.ast.Identifier
import printscript.model.ast.expression.Expression
import printscript.model.ast.statement.AssignmentStatement
import printscript.model.ast.statement.Statement
import printscript.model.source.SourceSpan
import printscript.parser.internal.ParsingContext
import printscript.parser.internal.ParsingResult
import printscript.parser.internal.TokenLookahead
import printscript.parser.internal.expression.ExpressionParser
import printscript.parser.internal.orReturn
import printscript.token.Token
import printscript.token.TokenType

private const val ASSIGN_OPERATOR_OFFSET = 1

private val ASSIGNMENT_START_TOKENS = setOf(
    TokenType.IDENTIFIER,
)

private val ASSIGN_OPERATOR_TOKENS = setOf(
    TokenType.ASSIGN,
)

internal class AssignmentParser(
    private val expressionParser: ExpressionParser,
) : StatementParser {

    override fun match(
        lookahead: TokenLookahead,
    ): StatementMatch {
        val firstToken = when (val result = lookahead.peek()) {
            is ParsingResult.Success -> {
                result.value
            }

            is ParsingResult.Failure -> {
                return StatementMatch.Failure(result.error)
            }
        }

        if (firstToken.type != TokenType.IDENTIFIER) {
            return StatementMatch.NoMatch(
                StatementMismatch.atCurrentToken(
                    expected = ASSIGNMENT_START_TOKENS,
                    actual = firstToken,
                ),
            )
        }

        val secondToken = when (
            val result = lookahead.peekAt(ASSIGN_OPERATOR_OFFSET)
        ) {
            is ParsingResult.Success -> {
                result.value
            }

            is ParsingResult.Failure -> {
                return StatementMatch.Failure(result.error)
            }
        }

        return if (secondToken.type == TokenType.ASSIGN) {
            StatementMatch.Match
        } else {
            StatementMatch.NoMatch(
                StatementMismatch(
                    lookaheadOffset = ASSIGN_OPERATOR_OFFSET,
                    expected = ASSIGN_OPERATOR_TOKENS,
                    actual = secondToken,
                ),
            )
        }
    }

    override fun parse(
        context: ParsingContext,
    ): ParsingResult<Statement> {
        val parts = parseParts(context)
            .orReturn { return it }

        return ParsingResult.Success(build(parts))
    }

    private fun parseParts(
        context: ParsingContext,
    ): ParsingResult<Parts> {
        val identifierToken =
            context.expect(TokenType.IDENTIFIER)
                .orReturn { return it }

        context.expect(TokenType.ASSIGN)
            .orReturn { return it }

        val expression =
            expressionParser.parse(context)
                .orReturn { return it }

        val semicolonToken =
            context.expect(TokenType.SEMICOLON)
                .orReturn { return it }

        return ParsingResult.Success(
            Parts(
                identifierToken = identifierToken,
                expression = expression,
                semicolonToken = semicolonToken,
            ),
        )
    }

    private fun build(
        parts: Parts,
    ): Statement {
        return AssignmentStatement(
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
    }

    private data class Parts(
        val identifierToken: Token,
        val expression: Expression,
        val semicolonToken: Token,
    )
}