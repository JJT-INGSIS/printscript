package printscript.parser.internal.statement

import printscript.model.ast.statement.Statement
import printscript.parser.internal.ParsingContext
import printscript.parser.internal.ParsingResult
import printscript.statement.ParseError

internal class StatementParserDispatcher(
    private val parsers: List<StatementParser>,
) {

    fun parse(
        context: ParsingContext,
    ): ParsingResult<Statement> {
        val mismatches = mutableListOf<StatementMismatch>()

        for (parser in parsers) {
            when (val match = parser.match(context)) {
                StatementMatch.Match -> {
                    return parser.parse(context)
                }

                is StatementMatch.NoMatch -> {
                    mismatches.add(match.mismatch)
                }

                is StatementMatch.Failure -> {
                    return ParsingResult.Failure(match.error)
                }
            }
        }

        return noMatchingStatement(mismatches)
    }

    private fun noMatchingStatement(
        mismatches: List<StatementMismatch>,
    ): ParsingResult.Failure {
        val furthestOffset = mismatches.maxOf {
            it.lookaheadOffset
        }

        val furthestMismatches = mismatches.filter {
            it.lookaheadOffset == furthestOffset
        }

        val actualToken = furthestMismatches.first().actual

        val expectedTokens = furthestMismatches
            .flatMap { it.expected }
            .toSet()

        return ParsingResult.Failure(
            ParseError.UnexpectedToken(
                expected = expectedTokens,
                actual = actualToken,
            ),
        )
    }
}