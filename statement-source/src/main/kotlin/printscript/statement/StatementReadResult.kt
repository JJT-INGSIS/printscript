package printscript.statement

import printscript.ast.statement.Statement

sealed interface StatementReadResult {

    /**
     * Trae la sentencia y la fuente para seguir leyendo: es el único
     * caso que continúa.
     */
    data class Success(
        val statement: Statement,
        val remainingSource: StatementSource,
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
