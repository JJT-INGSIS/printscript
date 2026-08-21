package printscript.source

interface SourceReader {

    /**
     * Returns the next source chunk and the reader representing
     * the remaining input without modifying this reader.
     */
    fun readChunk(): SourceChunkReadResult
}