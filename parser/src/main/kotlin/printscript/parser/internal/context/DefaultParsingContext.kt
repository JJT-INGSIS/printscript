package printscript.parser.internal.context

import printscript.ast.statement.Statement
import printscript.parser.ParsingContext
import printscript.parser.ParsingResult
import printscript.parser.internal.statement.StatementParserDispatcher
import printscript.parser.orReturn
import printscript.statement.ParseError
import printscript.token.Token
import printscript.token.TokenType

internal data class DefaultParsingContext(
    private val cursor: TokenCursor,
    private val statementParserDispatcher: StatementParserDispatcher,
) : ParsingContext {

    override fun peek(): ParsingResult<Token> {
        return toParsingResult(cursor.peek())
    }

    override fun consume(): ParsingResult<Token> {
        return toParsingResult(cursor.advance())
    }

    override fun expect(expected: Set<TokenType>): ParsingResult<Token> {
        val peeked = peek()
            .orReturn { return it }

        return acceptExpected(expected = expected, peeked = peeked)
    }

    override fun parseStatement(): ParsingResult<Statement> {
        return statementParserDispatcher.parseStatement(this)
    }

    private fun acceptExpected(expected: Set<TokenType>, peeked: ParsingResult.Success<Token>): ParsingResult<Token> {
        return if (peeked.value.type in expected) {
            peeked.resultingContext.consume()
        } else {
            unexpectedToken(expected = expected, actual = peeked.value)
        }
    }

    private fun unexpectedToken(expected: Set<TokenType>, actual: Token): ParsingResult.Failure {
        return ParsingResult.Failure(
            ParseError.UnexpectedToken(
                expected = expected,
                actual = actual,
            ),
        )
    }

    private fun toParsingResult(read: TokenCursorReadResult): ParsingResult<Token> {
        return when (read) {
            is TokenCursorReadResult.Success -> ParsingResult.Success(
                value = read.token,
                resultingContext = withCursor(read.resultingCursor),
            )

            is TokenCursorReadResult.Failure -> ParsingResult.Failure(
                ParseError.Lexical(read.error),
            )
        }
    }

    private fun withCursor(cursor: TokenCursor): ParsingContext {
        return copy(cursor = cursor)
    }
}
