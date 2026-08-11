package printscript.parser.internal


import printscript.statement.ParseError
import printscript.statement.StatementReadResult
import printscript.statement.StatementSource
import printscript.token.Token
import printscript.token.TokenType

internal class ParsingStatementSource(
    private val context: ParsingContext,
    private val synchronizer: PanicModeSynchronizer,
) : StatementSource {

    override fun nextStatement(): StatementReadResult =
        when (val lookahead = context.peek()) {
            is ParsingResult.Success -> {
                parseOrEnd(lookahead.value)
            }

            is ParsingResult.Failure -> {
                recoverFrom(lookahead.error)
            }
        }

    private fun parseOrEnd(
        token: Token,
    ): StatementReadResult {
        return if (token.type == TokenType.EOF) {
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
                recoverFrom(result.error)
            }
        }

    private fun recoverFrom(
        error: ParseError,
    ): StatementReadResult.Failure {
        synchronizer.synchronize(context)
        return StatementReadResult.Failure(error)
    }
}