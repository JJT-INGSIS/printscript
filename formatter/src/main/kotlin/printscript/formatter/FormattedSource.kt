package printscript.formatter

public interface FormattedSource {

    /**
     * Returns the next formatted statement.
     *
     * Failure and EndOfInput are terminal results.
     */
    public fun nextFormattedStatement(): FormattedStatementReadResult
}
