package printscript.source.internal

import printscript.source.SourceChunk
import printscript.source.SourceChunkReadResult
import printscript.source.SourceReader
import java.nio.file.Path

internal data class FileSourceReader(
    private val path: Path,
    private val nextByteOffset: Long,
    private val bufferSizeInBytes: Int,
) : SourceReader {

    override fun readChunk(): SourceChunkReadResult {
        return when (
            val loadResult = Utf8FileChunkReader.read(
                path = path,
                byteOffset = nextByteOffset,
                bufferSizeInBytes = bufferSizeInBytes,
            )
        ) {
            is FileChunkLoadResult.Success -> successfulRead(loadResult)

            is FileChunkLoadResult.Failure -> SourceChunkReadResult.Failure(
                error = loadResult.error,
                remainingReader = this,
            )

            FileChunkLoadResult.EndOfInput -> SourceChunkReadResult.EndOfInput
        }
    }

    private fun successfulRead(loadResult: FileChunkLoadResult.Success): SourceChunkReadResult.Success {
        return SourceChunkReadResult.Success(
            chunk = SourceChunk(loadResult.content),
            remainingReader = copy(
                nextByteOffset = nextByteOffset + loadResult.consumedByteCount,
            ),
        )
    }
}
