package printscript.parser.internal

import printscript.parser.internal.context.ParsingContext
import printscript.statement.StatementReadResult
import printscript.statement.StatementSource
import printscript.token.Token
import printscript.token.TokenType

internal data class ParsingStatementSource(
    private val context: ParsingContext,
    private val endOfInputTokenType: TokenType,
) : StatementSource {

    override fun nextStatement(): StatementReadResult {
        return when (val lookahead = context.peek()) {
            is ParsingResult.Success -> readFromToken(lookahead)

            is ParsingResult.Failure -> StatementReadResult.Failure(
                error = lookahead.error,
            )
        }
    }

    private fun readFromToken(lookahead: ParsingResult.Success<Token>): StatementReadResult {
        return if (lookahead.value.type == endOfInputTokenType) {
            StatementReadResult.EndOfInput
        } else {
            parseNextStatement(lookahead.resultingContext)
        }
    }

    private fun parseNextStatement(context: ParsingContext): StatementReadResult {
        return when (val result = context.parseStatement()) {
            is ParsingResult.Success -> StatementReadResult.Success(
                statement = result.value,
                remainingSource = withContext(result.resultingContext),
            )

            is ParsingResult.Failure -> StatementReadResult.Failure(
                error = result.error,
            )
        }
    }

    private fun withContext(context: ParsingContext): StatementSource {
        return copy(context = context)
    }
}
