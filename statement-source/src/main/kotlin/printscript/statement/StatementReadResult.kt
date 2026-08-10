package printscript.statement

import printscript.model.ast.statement.Statement

sealed interface StatementReadResult {

    data class Success(
        val statement: Statement,
    ) : StatementReadResult

    data class Failure(
        val error: ParseError,
    ) : StatementReadResult

    data object EndOfInput : StatementReadResult
}