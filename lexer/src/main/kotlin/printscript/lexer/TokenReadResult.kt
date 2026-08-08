package printscript.lexer

sealed interface TokenReadResult {
    data class Success(
        val token: Token
    ) : TokenReadResult

    data class Failure(
        val error: LexicalError
    ) : TokenReadResult
}