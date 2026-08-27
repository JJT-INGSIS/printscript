package printscript.lexer.internal

import printscript.lexer.Lexer
import printscript.lexer.internal.scanner.TokenScannerDispatcher
import printscript.lexer.scanning.IgnoredCharacterPolicy
import printscript.lexer.scanning.TokenScanner
import printscript.source.SourceReader
import printscript.token.TokenSource
import printscript.token.TokenType

internal class ScanningLexer(
    tokenScanners: List<TokenScanner>,
    private val ignoredCharacterPolicy: IgnoredCharacterPolicy,
    private val endOfInputTokenType: TokenType,
) : Lexer {

    private val tokenScannerDispatcher =
        TokenScannerDispatcher(
            scanners = tokenScanners,
        )

    override fun tokenize(sourceReader: SourceReader): TokenSource {
        return ScanningTokenSource(
            characterCursor =
            CharacterCursor.initial(
                sourceReader = sourceReader,
            ),
            tokenScannerDispatcher = tokenScannerDispatcher,
            ignoredCharacterPolicy = ignoredCharacterPolicy,
            endOfInputTokenType = endOfInputTokenType,
        )
    }
}
