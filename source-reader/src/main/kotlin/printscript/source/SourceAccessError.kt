package printscript.source

import java.nio.file.Path

public sealed interface SourceAccessError : SourceReaderCreationError, SourceReadError {

    public val path: Path

    public data class NotFound(
        override val path: Path,
    ) : SourceAccessError

    public data class NotAFile(
        override val path: Path,
    ) : SourceAccessError

    public data class NotReadable(
        override val path: Path,
    ) : SourceAccessError

    public data class ReadFailed(
        override val path: Path,
        public val reason: String,
    ) : SourceAccessError
}
