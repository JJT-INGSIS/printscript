package printscript.source

public sealed interface SourceReaderCreationError {

    public data class InvalidBufferSize(
        public val providedSizeInBytes: Int,
    ) : SourceReaderCreationError
}
