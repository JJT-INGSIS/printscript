package printscript.source.internal

import printscript.source.SourceReadError

internal sealed interface FileChunkLoadResult {

    data class Success(
        val content: String,
        val consumedByteCount: Long,
    ) : FileChunkLoadResult

    data class Failure(
        val error: SourceReadError,
    ) : FileChunkLoadResult

    data object EndOfInput : FileChunkLoadResult
}
