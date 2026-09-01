package printscript.token

public sealed interface TokenReadResult {

    public val remainingSource: TokenSource

    public data class Success(
        public val token: Token,
        override val remainingSource: TokenSource,
    ) : TokenReadResult

    public data class Failure(
        public val error: TokenReadError,
        override val remainingSource: TokenSource,
    ) : TokenReadResult
}
