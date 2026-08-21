package printscript.token



sealed interface TokenReadResult {

    val remainingSource: TokenSource

    data class Success(
        val token: Token,
       override val remainingSource: TokenSource,
    ) : TokenReadResult

    data class Failure(
        val error: LexicalError,
       override val remainingSource: TokenSource,
    ) : TokenReadResult
}