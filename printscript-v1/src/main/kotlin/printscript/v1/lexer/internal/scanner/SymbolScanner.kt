package printscript.v1.lexer.internal.scanner

import printscript.lexer.scanning.ScannerCursor
import printscript.lexer.scanning.TokenScanResult
import printscript.lexer.scanning.TokenScanner
import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan
import printscript.token.LexicalError
import printscript.token.Token
import printscript.token.TokenType

internal class SymbolScanner(
    tokenTypeByLexeme: Map<String, TokenType>,
) : TokenScanner {

    private val tokenTypeByLexeme: Map<String, TokenType> =
        tokenTypeByLexeme.toMap()

    override fun canStartWith(character: Char): Boolean {
        return tokenTypeByLexeme.containsKey(
            character.toString(),
        )
    }

    override fun scan(cursor: ScannerCursor, startingCharacter: Char): TokenScanResult {
        val startPosition = cursor.position
        val symbolLexeme = startingCharacter.toString()
        val resultingCursor =
            cursor.advance().resultingCursor

        val matchedTokenType =
            tokenTypeByLexeme[symbolLexeme]

        return createScanResult(
            tokenType = matchedTokenType,
            lexeme = symbolLexeme,
            character = startingCharacter,
            startPosition = startPosition,
            resultingCursor = resultingCursor,
        )
    }

    private fun createScanResult(
        tokenType: TokenType?,
        lexeme: String,
        character: Char,
        startPosition: SourcePosition,
        resultingCursor: ScannerCursor,
    ): TokenScanResult {
        val span = SourceSpan(
            start = startPosition,
            end = resultingCursor.position,
        )

        if (tokenType != null) {
            return TokenScanResult.Success(
                token = Token(
                    type = tokenType,
                    lexeme = lexeme,
                    span = span,
                ),
                resultingCursor = resultingCursor,
            )
        }

        return TokenScanResult.Failure(
            error = LexicalError.UnexpectedCharacter(
                character = character,
                span = span,
            ),
            resultingCursor = resultingCursor,
        )
    }
}
