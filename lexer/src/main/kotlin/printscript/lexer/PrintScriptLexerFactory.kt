package printscript.lexer

import printscript.lexer.internal.printScriptV1KeywordTokenTypesByLexeme
import printscript.lexer.internal.printScriptV1StringQuoteDelimiters
import printscript.lexer.internal.printScriptV1SymbolTokenTypesByLexeme
import printscript.lexer.internal.scanner.IdentifierOrKeywordScanner
import printscript.lexer.internal.scanner.NumberLiteralScanner
import printscript.lexer.internal.scanner.StringLiteralScanner
import printscript.lexer.internal.scanner.SymbolScanner
import printscript.token.PrintScriptV1TokenType

public object PrintScriptLexerFactory {

    public fun createV1(): Lexer {
        return PrintScriptLexer(
            endOfInputTokenType = PrintScriptV1TokenType.EOF,
            tokenScanners = listOf(
                StringLiteralScanner(
                    supportedQuoteDelimiters =
                    printScriptV1StringQuoteDelimiters,
                    stringLiteralTokenType =
                    PrintScriptV1TokenType.STRING_LITERAL,
                ),
                NumberLiteralScanner(
                    numberLiteralTokenType =
                    PrintScriptV1TokenType.NUMBER_LITERAL,
                ),
                IdentifierOrKeywordScanner(
                    keywordTokenTypesByLexeme =
                    printScriptV1KeywordTokenTypesByLexeme,
                    identifierTokenType =
                    PrintScriptV1TokenType.IDENTIFIER,
                ),
                SymbolScanner(
                    printScriptV1SymbolTokenTypesByLexeme,
                ),
            ),
        )
    }
}
