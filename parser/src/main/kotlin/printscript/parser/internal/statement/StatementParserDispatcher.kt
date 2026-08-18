package printscript.parser.internal.statement

import printscript.ast.statement.Statement
import printscript.parser.internal.context.ParsingContext
import printscript.parser.internal.ParsingResult
import printscript.parser.internal.orReturn
import printscript.statement.ParseError
import printscript.token.TokenType

/**
 * Elige qué parser se hace cargo de la próxima sentencia.
 *
 * Les pregunta a todos en orden hasta que uno la reclame. Ninguna de
 * esas preguntas consume tokens, así que todos miran la misma posición.
 *
 * Si nadie la reclama, arma el error con el desajuste del parser que
 * llegó más lejos: ese es el que mejor interpretó la intención de quien
 * escribió el código, y por lo tanto el que da el mensaje más útil.
 */
internal class StatementParserDispatcher(
    private val parsers: List<StatementParser>,
) {

    fun parseStatement(
        context: ParsingContext,
    ): ParsingResult<Statement> {
        val mismatches = mutableListOf<StatementMismatch>()

        for (parser in parsers) {
            when (val match = parser.matchInitialTokens(context)) {
                StatementMatch.Match -> {
                    return parser.parseStatement(context)
                }

                is StatementMatch.NoMatch -> {
                    mismatches.add(match.mismatch)
                }

                is StatementMatch.Failure -> {
                    return ParsingResult.Failure(match.error)
                }
            }
        }

        return unrecognizedStatementError(context, mismatches)
    }

    private fun unrecognizedStatementError(
        context: ParsingContext,
        mismatches: List<StatementMismatch>,
    ): ParsingResult<Statement> {
        val furthestOffset = mismatches.maxOfOrNull { it.lookaheadOffset }
            ?: return noParsersConfigured(context)

        val furthest = mismatches.filter {
            it.lookaheadOffset == furthestOffset
        }

        return ParsingResult.Failure(
            ParseError.UnexpectedToken(
                expected = expectedTokensOf(furthest),
                actual = furthest.first().actual,
            ),
        )
    }

    private fun noParsersConfigured(
        context: ParsingContext,
    ): ParsingResult<Statement> {
        val token = context.peek()
            .orReturn { return it }

        return ParsingResult.Failure(
            ParseError.UnexpectedToken(
                expected = emptySet(),
                actual = token,
            ),
        )
    }

    private fun expectedTokensOf(
        mismatches: List<StatementMismatch>,
    ): Set<TokenType> {
        return mismatches
            .flatMap { it.expected }
            .toSet()
    }
}