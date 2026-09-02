package printscript.source

import java.nio.file.Path

/**
 * Error produced while a source is being read.
 *
 * External [SourceReader] implementations may provide their own errors.
 */
public interface SourceReadError {

    public data class InvalidEncoding(
        public val path: Path,
        public val byteOffset: Long,
    ) : SourceReadError

    public data object InvalidInputStreamEncoding : SourceReadError

    public data class InputStreamReadFailed(
        public val reason: String,
    ) : SourceReadError
}
