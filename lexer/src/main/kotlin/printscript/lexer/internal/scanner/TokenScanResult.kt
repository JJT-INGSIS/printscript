package printscript.lexer.internal.scanner

import printscript.lexer.internal.CharacterCursor
import printscript.token.LexicalError
import printscript.token.Token

internal sealed interface TokenScanResult {

    val resultingCursor: CharacterCursor

    data class Success(
        val token: Token,
        override val resultingCursor: CharacterCursor,
    ) : TokenScanResult

    data class Failure(
        val error: LexicalError,
        override val resultingCursor: CharacterCursor,
    ) : TokenScanResult
}