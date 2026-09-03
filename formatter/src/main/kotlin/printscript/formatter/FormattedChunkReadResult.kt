package printscript.formatter

public sealed interface FormattedChunkReadResult {

    public data class Success(
        public val formattedText: String,
        public val remainingSource: FormattedSource,
    ) : FormattedChunkReadResult

    public data class Failure(
        public val error: FormattingError,
    ) : FormattedChunkReadResult

    public data object EndOfInput : FormattedChunkReadResult
}
