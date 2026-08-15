package printscript.lexer

import printscript.lexer.internal.ReaderCharacterCursor
import printscript.lexer.internal.ScanningTokenSource
import printscript.lexer.internal.scanner.TokenScanner
import printscript.lexer.internal.scanner.TokenScannerDispatcher
import printscript.token.TokenSource
import java.io.Reader

internal class PrintScriptLexer(
    tokenScanners: List<TokenScanner>,
) : Lexer {

    private val tokenScannerDispatcher = TokenScannerDispatcher(
        scanners = tokenScanners,
    )

    override fun tokenize(
        inputSource: Reader,
    ): TokenSource {
        val characterCursor = ReaderCharacterCursor(
            inputReader = inputSource,
        )

        return ScanningTokenSource(
            characterCursor = characterCursor,
            tokenScannerDispatcher = tokenScannerDispatcher,
        )
    }
}