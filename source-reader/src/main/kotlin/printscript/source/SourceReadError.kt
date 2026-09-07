package printscript.source

public interface SourceReadError {

    public data object InvalidInputStreamEncoding : SourceReadError

    public data class InputStreamReadFailed(
        public val reason: String,
    ) : SourceReadError
}
