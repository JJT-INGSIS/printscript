package printscript.v1.lexer.internal.scanner

import printscript.lexer.scanning.ScannerCursor
import printscript.lexer.scanning.TokenScanResult
import printscript.lexer.scanning.TokenScanner
import printscript.model.source.SourceSpan
import printscript.token.Token
import printscript.token.TokenType

internal class SymbolScanner(
    tokenTypeByCharacter: Map<Char, TokenType>,
) : TokenScanner {

    private val tokenTypeByCharacter: Map<Char, TokenType> =
        tokenTypeByCharacter.toMap()

    override fun canStartWith(character: Char): Boolean {
        return tokenTypeByCharacter.containsKey(character)
    }

    override fun scan(cursor: ScannerCursor, startingCharacter: Char): TokenScanResult {
        val startPosition = cursor.position
        val resultingCursor =
            cursor.advance().resultingCursor

        return TokenScanResult.Success(
            token = Token(
                type = tokenTypeByCharacter.getValue(startingCharacter),
                lexeme = startingCharacter.toString(),
                span = SourceSpan(
                    start = startPosition,
                    end = resultingCursor.position,
                ),
            ),
            resultingCursor = resultingCursor,
        )
    }
}
