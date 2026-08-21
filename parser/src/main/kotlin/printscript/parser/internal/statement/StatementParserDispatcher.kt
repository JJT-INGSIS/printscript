package printscript.parser.internal.statement

import printscript.ast.statement.Statement
import printscript.parser.internal.ParsingResult
import printscript.parser.internal.context.ParsingContext
import printscript.parser.internal.orReturn
import printscript.statement.ParseError
import printscript.token.Token
import printscript.token.TokenType
internal class StatementParserDispatcher(
    parsers: List<StatementParser>,
) {

    private val parserByStartToken: Map<TokenType, StatementParser> =
        parsers.associateBy { it.startToken }

    fun parseStatement(
        context: ParsingContext,
    ): ParsingResult<Statement> {
        val peeked = context.peek()
            .orReturn { return it }

        val parser = parserByStartToken[peeked.value.type]
            ?: return unrecognizedStatement(peeked.value)

        return parser.parseStatement(peeked.resultingContext)
    }

    private fun unrecognizedStatement(
        token: Token,
    ): ParsingResult.Failure {
        return ParsingResult.Failure(
            ParseError.UnexpectedToken(
                expected = parserByStartToken.keys,
                actual = token,
            ),
        )
    }
}