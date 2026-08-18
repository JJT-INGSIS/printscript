package printscript.parser.internal

import printscript.parser.internal.context.ParsingContext
import printscript.statement.StatementReadResult
import printscript.statement.StatementSource
import printscript.token.Token
import printscript.token.TokenType

internal class ParsingStatementSource(
    private val context: ParsingContext,
) : StatementSource {

    override fun nextStatement(): StatementReadResult {
        return when (val lookahead = context.peek()) {
            is ParsingResult.Success ->
                readFromToken(lookahead.value)

            is ParsingResult.Failure ->
                StatementReadResult.Failure(
                    error = lookahead.error,
                )
        }
    }

    private fun readFromToken(
        token: Token,
    ): StatementReadResult {
        if (token.type == TokenType.EOF) {
            return StatementReadResult.EndOfInput
        }

        return parseNextStatement()
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
