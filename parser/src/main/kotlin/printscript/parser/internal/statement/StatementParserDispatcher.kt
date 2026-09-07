package printscript.parser.internal.statement

import printscript.parser.ParsingContext
import printscript.parser.ParsingResult
import printscript.parser.StatementParser
import printscript.parser.orReturn
import printscript.statement.ParseError
import printscript.statement.Statement
import printscript.token.Token
import printscript.token.TokenType

internal class StatementParserDispatcher(
    parsers: List<StatementParser>,
) {

    private val parsers: List<StatementParser> =
        parsers.toList()

    private val expectedStartTokenTypes: Set<TokenType> =
        this.parsers
            .map { parser -> parser.startTokenType }
            .toSet()

    fun parseStatement(context: ParsingContext): ParsingResult<Statement> {
        val peeked = context.peek()
            .orReturn { return it }

        for (parser in parsers) {
            if (parser.startTokenType == peeked.value.type) {
                return parser.parseStatement(peeked.resultingContext)
            }
        }

        return createUnexpectedTokenFailure(peeked.value)
    }

    private fun createUnexpectedTokenFailure(token: Token): ParsingResult.Failure {
        return ParsingResult.Failure(
            error = ParseError.UnexpectedToken(
                expected = expectedStartTokenTypes,
                actual = token,
            ),
        )
    }
}
