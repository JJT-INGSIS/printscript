package printscript.parser.internal.statement

import printscript.model.ast.expression.Expression
import printscript.model.ast.statement.PrintlnStatement
import printscript.model.ast.statement.Statement
import printscript.model.source.SourceSpan
import printscript.parser.internal.ParsingContext
import printscript.parser.internal.ParsingResult
import printscript.parser.internal.TokenLookahead
import printscript.parser.internal.expression.ExpressionParser
import printscript.parser.internal.orReturn
import printscript.token.Token
import printscript.token.TokenType

internal class PrintlnParser(
    private val expressionParser: ExpressionParser,
) : StatementParser {

    override fun match(
        lookahead: TokenLookahead,
    ): StatementMatch {
        return when (val result = lookahead.peek()) {
            is ParsingResult.Success -> {
                val token = result.value

                if (token.type == TokenType.PRINTLN) {
                    StatementMatch.Match
                } else {
                    StatementMatch.NoMatch(
                        StatementMismatch.atCurrentToken(
                            expected = PRINTLN_START_TOKENS,
                            actual = token,
                        ),
                    )
                }
            }

            is ParsingResult.Failure -> {
                StatementMatch.Failure(result.error)
            }
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
        val printlnToken =
            context.expect(TokenType.PRINTLN)
                .orReturn { return it }

        context.expect(TokenType.LEFT_PAREN)
            .orReturn { return it }

        val argument =
            expressionParser.parse(context)
                .orReturn { return it }

        context.expect(TokenType.RIGHT_PAREN)
            .orReturn { return it }

        val semicolonToken =
            context.expect(TokenType.SEMICOLON)
                .orReturn { return it }

        return ParsingResult.Success(
            Parts(
                printlnToken = printlnToken,
                argument = argument,
                semicolonToken = semicolonToken,
            ),
        )
    }

    private fun build(
        parts: Parts,
    ): Statement {
        return PrintlnStatement(
            argument = parts.argument,
            span = SourceSpan(
                start = parts.printlnToken.span.start,
                end = parts.semicolonToken.span.end,
            ),
        )
    }

    private data class Parts(
        val printlnToken: Token,
        val argument: Expression,
        val semicolonToken: Token,
    )

    private companion object {
        val PRINTLN_START_TOKENS = setOf(
            TokenType.PRINTLN,
        )
    }
}