package printscript.source

import printscript.source.internal.FileSourceReader
import printscript.source.internal.InputStreamSourceReader
import printscript.source.internal.StringSourceReader
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path

private const val DEFAULT_FILE_BUFFER_SIZE_IN_BYTES = 8_192
private const val DEFAULT_INPUT_STREAM_BUFFER_SIZE_IN_CHARACTERS = 8_192
private const val DEFAULT_STRING_CHUNK_SIZE_IN_CHARACTERS = 8_192

public object SourceReaderFactory {

    public fun fromString(sourceCode: String): SourceReader {
        return StringSourceReader(
            sourceCode = sourceCode,
            nextOffset = 0,
            chunkSize = DEFAULT_STRING_CHUNK_SIZE_IN_CHARACTERS,
        )
    }

    /**
     * Creates a lazy, single-pass reader over [inputStream].
     *
     * This factory does not consume or close the stream. The caller retains
     * ownership and must close it after the complete pipeline has finished.
     */
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

    public fun fromPath(
        path: Path,
        bufferSizeInBytes: Int = DEFAULT_FILE_BUFFER_SIZE_IN_BYTES,
    ): SourceReaderCreationResult {
        if (bufferSizeInBytes <= 0) {
            return SourceReaderCreationResult.Failure(
                SourceReaderCreationError.InvalidBufferSize(bufferSizeInBytes),
            )
        }

        if (!Files.exists(path)) {
            return SourceReaderCreationResult.Failure(SourceAccessError.NotFound(path))
        }

        if (!Files.isRegularFile(path)) {
            return SourceReaderCreationResult.Failure(SourceAccessError.NotAFile(path))
        }

        if (!Files.isReadable(path)) {
            return SourceReaderCreationResult.Failure(SourceAccessError.NotReadable(path))
        }

        return SourceReaderCreationResult.Success(
            FileSourceReader(
                path = path,
                nextByteOffset = 0L,
                bufferSizeInBytes = bufferSizeInBytes,
            ),
        )
    }
}
