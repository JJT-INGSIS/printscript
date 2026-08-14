package printscript.statement

interface StatementSource {
    /**
     * Reads the next statement.
     *
     * Failure is terminal: the source stops at the first error,
     * and every later call returns EndOfInput.
     */
    fun nextStatement(): StatementReadResult
}