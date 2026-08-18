package printscript.parser.internal

import printscript.parser.internal.context.ParsingContext
import printscript.statement.StatementReadResult
import printscript.statement.StatementSource
import printscript.token.TokenType

internal class ParsingStatementSource(
    private val context: ParsingContext,
) : StatementSource {

    override fun nextStatement(): StatementReadResult {
        return when (val result = context.peek()) {
            is ParsingResult.Success -> {
                if (result.value.type == TokenType.EOF) {
                    StatementReadResult.EndOfInput
                } else {
                    parseNextStatement()
                }
            }

            is ParsingResult.Failure ->
                StatementReadResult.Failure(
                    error = result.error,
                )
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
}