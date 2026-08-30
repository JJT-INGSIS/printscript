package printscript.v1.parser.internal.statement

import printscript.ast.Identifier
import printscript.parser.ParsingContext
import printscript.parser.ParsingResult
import printscript.parser.StatementParser
import printscript.parser.orReturn
import printscript.statement.ParseError
import printscript.statement.Statement
import printscript.token.Token
import printscript.token.TokenType

internal class IdentifierStatementParser(
    parsers: List<TargetedStatementParser>,
    override val startTokenType: TokenType,
) : StatementParser {

    private val parserByFollowingToken: Map<TokenType, TargetedStatementParser> =
        parsers.associateBy { parser -> parser.followingTokenType }

    override fun parseStatement(context: ParsingContext): ParsingResult<Statement> {
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

    private fun readTarget(context: ParsingContext): ParsingResult<Identifier> {
        val targetToken = context.expect(startTokenType)
            .orReturn { return it }

        return ParsingResult.Success(
            value = Identifier(
                value = targetToken.value.lexeme,
                span = targetToken.value.span,
            ),
            resultingContext = targetToken.resultingContext,
        )
    }

    private fun unrecognizedStatement(token: Token): ParsingResult.Failure {
        return ParsingResult.Failure(
            ParseError.UnexpectedToken(
                expected = parserByFollowingToken.keys,
                actual = token,
            ),
        )
    }
}
