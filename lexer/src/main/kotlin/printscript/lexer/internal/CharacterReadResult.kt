package printscript.lexer.internal

internal sealed interface CharacterReadResult {

    val resultingCursor: CharacterCursor

    data class Success(
        val character: Char,
        override val resultingCursor: CharacterCursor,
    ) : CharacterReadResult

    data class EndOfInput(
        override val resultingCursor: CharacterCursor,
    ) : CharacterReadResult
}
