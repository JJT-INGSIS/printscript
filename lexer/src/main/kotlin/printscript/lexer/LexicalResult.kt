package printscript.lexer

sealed interface LexicalResult {
    data class Success(
        val token: Token
    ) : LexicalResult

    data class Failure(
        val error: LexicalError
    ) : LexicalResult
}