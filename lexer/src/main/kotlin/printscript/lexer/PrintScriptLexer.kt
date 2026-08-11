package printscript.lexer

import printscript.lexer.internal.ReaderCharacterCursor
import printscript.lexer.internal.ScanningTokenSource
import printscript.lexer.internal.printScriptV1FixedTokens
import printscript.lexer.internal.scanner.IdentifierOrKeywordScanner
import printscript.lexer.internal.scanner.NumberLiteralScanner
import printscript.lexer.internal.scanner.StringLiteralScanner
import printscript.lexer.internal.scanner.SymbolScanner
import printscript.lexer.internal.scanner.TokenScannerDispatcher
import printscript.token.TokenSource
import java.io.Reader

class PrintScriptLexer : Lexer {

    private val scannerDispatcher = TokenScannerDispatcher(
        scanners = listOf(
            StringLiteralScanner(),
            NumberLiteralScanner(),
            IdentifierOrKeywordScanner(printScriptV1FixedTokens),
            SymbolScanner(printScriptV1FixedTokens),
        ),
    )

    override fun tokenize(
        inputSource: Reader,
    ): TokenSource {
        val cursor = ReaderCharacterCursor(inputSource)

        return ScanningTokenSource(
            cursor = cursor,
            scannerDispatcher = scannerDispatcher,
        )
    }
}