package printscript.source.internal

import printscript.source.SourceChunk
import printscript.source.SourceChunkReadResult
import printscript.source.SourceReader

internal data class StringSourceReader(
    private val sourceCode: String,
    private val nextOffset: Int,
    private val chunkSize: Int,
) : SourceReader {

    override fun readChunk(): SourceChunkReadResult {
        if (nextOffset >= sourceCode.length) {
            return SourceChunkReadResult.EndOfInput
        }

        val chunkEndOffset = minOf(
            nextOffset + chunkSize,
            sourceCode.length,
        )

        return SourceChunkReadResult.Success(
            chunk = SourceChunk(
                content = sourceCode.substring(
                    startIndex = nextOffset,
                    endIndex = chunkEndOffset,
                ),
            ),
            remainingReader = copy(
                nextOffset = chunkEndOffset,
            ),
        )
    }
}