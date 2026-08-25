package printscript.lexer

import printscript.lexer.internal.CharacterCursor
import printscript.lexer.internal.ScanningTokenSource
import printscript.lexer.internal.scanner.TokenScanner
import printscript.lexer.internal.scanner.TokenScannerDispatcher
import printscript.source.SourceReader
import printscript.token.TokenSource
import printscript.token.TokenType

internal class PrintScriptLexer(
    tokenScanners: List<TokenScanner>,
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
            tokenScannerDispatcher =
            tokenScannerDispatcher,
            endOfInputTokenType = endOfInputTokenType,
        )
    }
}
