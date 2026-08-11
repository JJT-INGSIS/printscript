package printscript.statement

interface StatementSource {
    /**
     * Reads the next statement.
     *
     * Failure is not terminal. After a failure, the source
     * attempts to recover, and later calls may produce
     * Success, Failure, or EndOfInput.
     */
    fun nextStatement(): StatementReadResult
}