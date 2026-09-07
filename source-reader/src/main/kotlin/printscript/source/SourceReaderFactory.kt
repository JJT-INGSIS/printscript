package printscript.source

import printscript.source.internal.InputStreamSourceReader
import printscript.source.internal.StringSourceReader
import java.io.InputStream

private const val DEFAULT_INPUT_STREAM_BUFFER_SIZE_IN_CHARACTERS = 8_192
private const val DEFAULT_STRING_CHUNK_SIZE_IN_CHARACTERS = 8_192

public object SourceReaderFactory {

    @JvmStatic
    public fun fromString(sourceCode: String): SourceReader {
        return StringSourceReader(
            sourceCode = sourceCode,
            nextOffset = 0,
            chunkSize = DEFAULT_STRING_CHUNK_SIZE_IN_CHARACTERS,
        )
    }

    @JvmStatic
    @JvmOverloads
    public fun fromInputStream(
        inputStream: InputStream,
        bufferSizeInCharacters: Int = DEFAULT_INPUT_STREAM_BUFFER_SIZE_IN_CHARACTERS,
    ): SourceReaderCreationResult {
        if (bufferSizeInCharacters <= 0) {
            return SourceReaderCreationResult.Failure(
                SourceReaderCreationError.InvalidBufferSize(bufferSizeInCharacters),
            )
        }

        return SourceReaderCreationResult.Success(
            InputStreamSourceReader(
                inputStream = inputStream,
                bufferSizeInCharacters = bufferSizeInCharacters,
            ),
        )
    }
}
