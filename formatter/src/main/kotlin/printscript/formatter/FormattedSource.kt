package printscript.formatter

public interface FormattedSource {

    /**
     * Returns the next formatted chunk.
     *
     * Failure and EndOfInput are terminal results.
     */
    public fun nextFormattedChunk(): FormattedChunkReadResult
}
