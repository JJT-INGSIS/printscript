package printscript.statement

import printscript.model.ast.statement.Statement

sealed interface StatementReadResult {

    data class Success(
        val statement: Statement,
    ) : StatementReadResult

    /**
     * The current statement could not be parsed. This is
     * terminal: later calls return EndOfInput.
     */
    data class Failure(
        val error: ParseError,
    ) : StatementReadResult

    data object EndOfInput : StatementReadResult
}