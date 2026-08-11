package printscript.parser.internal

import printscript.model.ast.statement.Statement
import printscript.parser.internal.statement.StatementParserDispatcher
import printscript.statement.ParseError
import printscript.token.Token
import printscript.token.TokenReadResult
import printscript.token.TokenSource
import printscript.token.TokenType

internal class DefaultParsingContext(
    tokens: TokenSource,
    private val statementParserDispatcher: StatementParserDispatcher,
) : ParsingContext {

    private val cursor = TokenCursor(tokens)

    override fun peekAt(
        offset: Int,
    ): ParsingResult<Token> {
        return cursor.peekAt(offset).toParsingResult()
    }

    override fun consume(): ParsingResult<Token> {
        return cursor.advance().toParsingResult()
    }

    override fun expect(
        expected: Set<TokenType>,
    ): ParsingResult<Token> {
        val token = peek()
            .orReturn { return it }

        if (token.type !in expected) {
            return ParsingResult.Failure(
                ParseError.UnexpectedToken(
                    expected = expected,
                    actual = token,
                ),
            )
        }

        return consume()
    }

    override fun parseStatement(): ParsingResult<Statement> {
        return statementParserDispatcher.parse(this)
    }

    private fun TokenReadResult.toParsingResult(): ParsingResult<Token> {
        return when (this) {
            is TokenReadResult.Success -> {
                ParsingResult.Success(token)
            }

            is TokenReadResult.Failure -> {
                ParsingResult.Failure(
                    ParseError.Lexical(error),
                )
            }
        }
    }
}