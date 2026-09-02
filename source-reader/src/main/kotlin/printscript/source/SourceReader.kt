package printscript.source

public interface SourceReader {

    /**
     * Reads the next source chunk.
     *
     * Consumers must continue exclusively from the returned remaining reader.
     * Replayable inputs may implement that continuation as an immutable state;
     * non-replayable streams represent a linear continuation over the same
     * underlying resource.
     */
    public fun readChunk(): SourceChunkReadResult
}
