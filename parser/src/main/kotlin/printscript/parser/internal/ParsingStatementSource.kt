package printscript.parser.internal

import printscript.model.ast.statement.Statement
import printscript.parser.internal.statement.StatementParser
import printscript.statement.ParseError
import printscript.statement.StatementReadResult
import printscript.statement.StatementSource
import printscript.token.Token
import printscript.token.TokenReadResult
import printscript.token.TokenSource
import printscript.token.TokenType

internal class ParsingStatementSource(
    tokens: TokenSource,
    private val statementParsers: List<StatementParser>,
) : StatementSource, ParsingContext {

    private val cursor = TokenCursor(tokens)

    override fun nextStatement(): StatementReadResult =
        if (isEndOfInput()) endOfInput()
        else parseNextStatement()

    private fun isEndOfInput(): Boolean {
        val nextToken = peek().orReturn { return false }
        return nextToken.type == TokenType.EOF
    }

    private fun endOfInput(): StatementReadResult = StatementReadResult.EndOfInput

    private fun parseNextStatement(): StatementReadResult =
        when (val result = parseStatement()) {
            is ParsingResult.Success -> StatementReadResult.Success(result.value)
            is ParsingResult.Failure -> {
                synchronize()
                StatementReadResult.Failure(result.error)
            }
        }

    private fun synchronize() {
        while (true) {
            when (val result = peek()) {
                is ParsingResult.Failure -> consume()
                is ParsingResult.Success -> {
                    if (result.value.type == TokenType.EOF) return
                    consume()
                    if (result.value.type == TokenType.SEMICOLON) return
                }
            }
        }
    }

    override fun parseStatement(): ParsingResult<Statement> {
        val token = peek().orReturn { return it }
        val statementParser = statementParsers.firstOrNull { it.canParse(this) }
            ?: return ParsingResult.Failure(
                ParseError.UnexpectedToken(expected = emptySet(), actual = token),
            )
        return statementParser.parse(this)
    }

    override fun peekAt(offset: Int): ParsingResult<Token> = cursor.peekAt(offset).toParsingResult()

    override fun consume(): ParsingResult<Token> = cursor.advance().toParsingResult()

    override fun expect(expected: Set<TokenType>): ParsingResult<Token> {
        val token = peek().orReturn { return it }
        if (token.type !in expected) {
            return ParsingResult.Failure(
                ParseError.UnexpectedToken(expected = expected, actual = token),
            )
        }
        return consume()
    }

    private fun TokenReadResult.toParsingResult(): ParsingResult<Token> =
        when (this) {
            is TokenReadResult.Success -> ParsingResult.Success(token)
            is TokenReadResult.Failure -> ParsingResult.Failure(ParseError.Lexical(error))
        }
}
