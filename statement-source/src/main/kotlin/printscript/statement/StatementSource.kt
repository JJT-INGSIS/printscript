package printscript.statement

public interface StatementSource {

    /**
     * Reads the next statement.
     *
     * Failure and EndOfInput are terminal results.
     * Consumers must stop reading after receiving either one.
     */
    public fun nextStatement(): StatementReadResult
}
