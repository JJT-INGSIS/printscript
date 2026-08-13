package printscript.parser.internal


import printscript.statement.ParseError
import printscript.statement.StatementReadResult
import printscript.statement.StatementSource
import printscript.token.Token
import printscript.token.TokenType

internal class ParsingStatementSource(
    private val context: ParsingContext,
) : StatementSource {

    private var finished = false

    override fun nextStatement(): StatementReadResult {
        if (finished) {
            return StatementReadResult.EndOfInput
        }

        return when (val lookahead = context.peek()) {
            is ParsingResult.Success -> {
                parseOrEnd(lookahead.value)
            }

            is ParsingResult.Failure -> {
                fail(lookahead.error)
            }
        }
    }

    private fun parseOrEnd(
        token: Token,
    ): StatementReadResult {
        return if (token.type == TokenType.EOF) {
            finish()
            StatementReadResult.EndOfInput
        } else {
            parseNextStatement()
        }
    }

    private fun parseNextStatement(): StatementReadResult =
        when (val result = context.parseStatement()) {
            is ParsingResult.Success -> {
                StatementReadResult.Success(result.value)
            }

            is ParsingResult.Failure -> {
                fail(result.error)
            }
        }

    private fun fail(
        error: ParseError,
    ): StatementReadResult.Failure {
        finish()
        return StatementReadResult.Failure(error)
    }

    private fun finish() {
        finished = true
    }
}
