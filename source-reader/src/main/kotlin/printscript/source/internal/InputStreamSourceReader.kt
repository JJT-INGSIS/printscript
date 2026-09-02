package printscript.source.internal

import printscript.source.SourceChunk
import printscript.source.SourceChunkReadResult
import printscript.source.SourceReadError
import printscript.source.SourceReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.CharacterCodingException
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets

internal class InputStreamSourceReader(
    inputStream: InputStream,
    private val bufferSizeInCharacters: Int,
) : SourceReader {

    private val characterReader = InputStreamReader(
        inputStream,
        StandardCharsets.UTF_8
            .newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT),
    )

    override fun readChunk(): SourceChunkReadResult {
        val buffer = CharArray(bufferSizeInCharacters)

        return try {
            readInto(buffer)
        } catch (_: CharacterCodingException) {
            SourceChunkReadResult.Failure(
                error = SourceReadError.InvalidInputStreamEncoding,
                remainingReader = this,
            )
        } catch (cause: IOException) {
            SourceChunkReadResult.Failure(
                error = SourceReadError.InputStreamReadFailed(
                    reason = cause.message.orEmpty(),
                ),
                remainingReader = this,
            )
        }
    }

    private fun readInto(buffer: CharArray): SourceChunkReadResult {
        val readCharacterCount = characterReader.read(buffer)

        if (readCharacterCount == END_OF_INPUT) {
            return SourceChunkReadResult.EndOfInput
        }

        return SourceChunkReadResult.Success(
            chunk = SourceChunk(
                content = buffer.concatToString(
                    startIndex = 0,
                    endIndex = readCharacterCount,
                ),
            ),
            remainingReader = this,
        )
    }
}

private const val END_OF_INPUT = -1
