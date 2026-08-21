package printscript.source

sealed interface SourceChunkReadResult {

    data class Success(
        val chunk: SourceChunk,
        val remainingReader: SourceReader,
    ) : SourceChunkReadResult

    data object EndOfInput : SourceChunkReadResult
}