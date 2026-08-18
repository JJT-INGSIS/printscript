package printscript.statement

interface StatementSource {
    /**
     * Reads the next statement.
     *
     * Failure and EndOfInput are terminal results.
     * Consumers must stop reading after receiving either one.
     */
    fun nextStatement(): StatementReadResult
}