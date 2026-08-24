package printscript.formatter

public sealed interface FormattedStatementReadResult {

    public data class Success(
        public val formattedText: String,
        public val remainingSource: FormattedSource,
    ) : FormattedStatementReadResult

    /**
     * Terminal result. Consumers must stop reading.
     */
    public data class Failure(
        public val error: FormattingError,
    ) : FormattedStatementReadResult

    public data object EndOfInput : FormattedStatementReadResult
}
