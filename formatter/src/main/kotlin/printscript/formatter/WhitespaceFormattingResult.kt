package printscript.formatter

public sealed interface WhitespaceFormattingResult {

    public data class Success(public val whitespace: String) : WhitespaceFormattingResult

    public data class Failure(public val error: FormattingError) : WhitespaceFormattingResult
}
