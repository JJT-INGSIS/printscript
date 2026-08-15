package printscript.lexer

import printscript.lexer.internal.printScriptV1KeywordTokenTypesByLexeme
import printscript.lexer.internal.printScriptV1SymbolTokenTypesByLexeme
import printscript.lexer.internal.scanner.IdentifierOrKeywordScanner
import printscript.lexer.internal.scanner.NumberLiteralScanner
import printscript.lexer.internal.scanner.StringLiteralScanner
import printscript.lexer.internal.scanner.SymbolScanner

object PrintScriptLexerFactory {

    fun createV1(): Lexer {
        return PrintScriptLexer(
            tokenScanners = listOf(
                StringLiteralScanner(),
                NumberLiteralScanner(),
                IdentifierOrKeywordScanner(
                    printScriptV1KeywordTokenTypesByLexeme,
                ),
                SymbolScanner(
                    printScriptV1SymbolTokenTypesByLexeme,
                ),
            ),
        )
    }
}