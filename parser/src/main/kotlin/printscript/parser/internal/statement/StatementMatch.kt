package printscript.parser.internal.statement

import printscript.statement.ParseError

internal sealed interface StatementMatch {

    data object Match : StatementMatch

    data class NoMatch(
        val mismatch: StatementMismatch,
    ) : StatementMatch

    data class Failure(
        val error: ParseError,
    ) : StatementMatch
}