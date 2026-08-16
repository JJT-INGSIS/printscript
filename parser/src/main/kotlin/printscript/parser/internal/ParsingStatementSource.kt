package printscript.parser.internal

import printscript.parser.internal.context.ParsingContext
import printscript.statement.ParseError
import printscript.statement.StatementReadResult
import printscript.statement.StatementSource
import printscript.token.Token
import printscript.token.TokenType

/**
 * Entrega las sentencias del programa de a una, parseándolas a demanda.
 *
 * Corta en el primer error: una vez que reporta un Failure, las
 * llamadas siguientes devuelven EndOfInput. Un programa con un error
 * de sintaxis no se parsea más allá de ese punto.
 */
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
        if (token.type == TokenType.EOF) {
            finished = true
            return StatementReadResult.EndOfInput
        }

        return parseNextStatement()
    }

    private fun parseNextStatement(): StatementReadResult {
        return when (val result = context.parseStatement()) {
            is ParsingResult.Success -> {
                StatementReadResult.Success(result.value)
            }

            is ParsingResult.Failure -> {
                fail(result.error)
            }
        }
    }

    private fun fail(
        error: ParseError,
    ): StatementReadResult.Failure {
        finished = true
        return StatementReadResult.Failure(error)
    }
}