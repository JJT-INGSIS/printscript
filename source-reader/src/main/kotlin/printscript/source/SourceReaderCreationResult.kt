package printscript.source

public sealed interface SourceReaderCreationResult {

    public data class Success(
        public val reader: SourceReader,
    ) : SourceReaderCreationResult

    public data class Failure(
        public val error: SourceAccessError,
    ) : SourceReaderCreationResult
}
