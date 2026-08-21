package printscript.parser.internal.statement

import printscript.ast.Identifier
import printscript.ast.statement.Statement
import printscript.parser.internal.ParsingResult
import printscript.parser.internal.context.ParsingContext
import printscript.parser.internal.orReturn
import printscript.statement.ParseError
import printscript.token.Token
import printscript.token.TokenType

/**
 * Sentencias que arrancan con un identificador. Como todas comparten
 * ese primer token, lo lee una sola vez y la elección la decide el
 * token que le sigue.
 */
internal class IdentifierStatementParser(
    parsers: List<TargetedStatementParser>,
    override val startToken: TokenType = DEFAULT_START_TOKEN,
) : StatementParser {

    private val parserByFollowingToken: Map<TokenType, TargetedStatementParser> =
        parsers.associateBy { it.followingToken }

    override fun parseStatement(
        context: ParsingContext,
    ): ParsingResult<Statement> {
        val target = readTarget(context)
            .orReturn { return it }

        val following = target.resultingContext.peek()
            .orReturn { return it }

        val parser = parserByFollowingToken[following.value.type]
            ?: return unrecognizedStatement(following.value)

        return parser.parseStatement(
            target = target.value,
            context = following.resultingContext,
        )
    }

    private fun readTarget(
        context: ParsingContext,
    ): ParsingResult<Identifier> {
        val targetToken = context.expect(startToken)
            .orReturn { return it }

        return ParsingResult.Success(
            value = Identifier(
                value = targetToken.value.lexeme,
                span = targetToken.value.span,
            ),
            resultingContext = targetToken.resultingContext,
        )
    }

    private fun unrecognizedStatement(
        token: Token,
    ): ParsingResult.Failure {
        return ParsingResult.Failure(
            ParseError.UnexpectedToken(
                expected = parserByFollowingToken.keys,
                actual = token,
            ),
        )
    }

    private companion object {
        val DEFAULT_START_TOKEN = TokenType.IDENTIFIER
    }
}
