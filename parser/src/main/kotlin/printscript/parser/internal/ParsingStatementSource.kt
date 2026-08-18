package printscript.parser.internal

import printscript.parser.internal.context.ParsingContext
import printscript.statement.StatementReadResult
import printscript.statement.StatementSource
import printscript.token.TokenType

internal class ParsingStatementSource(
    private val context: ParsingContext,
) : StatementSource {

    private var finished = false // no es inmutable

    override fun nextStatement(): StatementReadResult {
        if (finished) {
            return StatementReadResult.EndOfInput // hacer q tenga una misma firma a lo largo del proyecto (interpreter)
        }

        return when (val lookahead = context.peek()) {
            is ParsingResult.Success -> {
                if (result.value.type == TokenType.EOF) {
                    StatementReadResult.EndOfInput
                } else {
                    parseNextStatement()
                }
            }

            is ParsingResult.Failure -> {
                fail(lookahead.error) // hacer un fail mas declarativo
            }
        }
    }

    private fun parseOrEnd(
        token: Token,
    ): StatementReadResult {
        if (token.type == TokenType.EOF) {
            finished = true
            return StatementReadResult.EndOfInput
        }
    }

    private fun parseNextStatement(): StatementReadResult {
        return when (val result = context.parseStatement()) {
            is ParsingResult.Success ->
                StatementReadResult.Success(
                    statement = result.value,
                )

            is ParsingResult.Failure ->
                StatementReadResult.Failure(
                    error = result.error,
                )
        }
    }

    private fun fail( // esto esta pinchi, arregalr todo esto
        error: ParseError,
    ): StatementReadResult.Failure {
        finished = true
        return StatementReadResult.Failure(error)
    }
}