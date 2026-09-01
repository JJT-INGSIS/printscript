package printscript.source

public sealed interface SourceChunkReadResult {

    public data class Success(
        public val chunk: SourceChunk,
        public val remainingReader: SourceReader,
    ) : SourceChunkReadResult

    public data class Failure(
        public val error: SourceReadError,
        public val remainingReader: SourceReader,
    ) : SourceChunkReadResult

    public data object EndOfInput : SourceChunkReadResult
}
