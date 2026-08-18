package printscript.statement

import printscript.ast.statement.Statement

sealed interface StatementReadResult {

    data class Success(
        val statement: Statement,
    ) : StatementReadResult

    /**
     * The next statement could not be parsed.
     *
     * This is a terminal result. Consumers must stop reading.
     */
    data class Failure(
        val error: ParseError,
    ) : StatementReadResult

    data object EndOfInput : StatementReadResult
}