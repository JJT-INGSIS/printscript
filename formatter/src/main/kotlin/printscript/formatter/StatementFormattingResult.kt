package printscript.formatter

public sealed interface StatementFormattingResult {

    public data class Success(
        public val formattedText: String,
    ) : StatementFormattingResult

    public data class Failure(
        public val error: FormattingError,
    ) : StatementFormattingResult
}
