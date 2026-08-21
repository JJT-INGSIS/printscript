package printscript.source

import printscript.source.internal.StringSourceReader

private const val DEFAULT_CHUNK_SIZE_IN_CHARACTERS = 8_192

public object SourceReaderFactory {

    public fun fromString(sourceCode: String): SourceReader {
        return StringSourceReader(
            sourceCode = sourceCode,
            nextOffset = 0,
            chunkSize = DEFAULT_CHUNK_SIZE_IN_CHARACTERS,
        )
    }
}
