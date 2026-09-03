package printscript.v1.lexer.internal

import printscript.lexer.scanning.TokenScanner
import printscript.v1.lexer.PrintScriptV1LexerConfiguration
import printscript.v1.lexer.internal.scanner.IdentifierOrKeywordScanner
import printscript.v1.lexer.internal.scanner.NumberLiteralScanner
import printscript.v1.lexer.internal.scanner.StringLiteralScanner
import printscript.v1.lexer.internal.scanner.SymbolScanner
import printscript.v1.token.PrintScriptV1TokenType

internal fun printScriptV1TokenScanners(configuration: PrintScriptV1LexerConfiguration): List<TokenScanner> {
    return listOf(
        StringLiteralScanner(
            supportedQuoteDelimiters = configuration.stringQuoteDelimiters,
            stringLiteralTokenType = PrintScriptV1TokenType.STRING_LITERAL,
        ),
        NumberLiteralScanner(
            numberLiteralTokenType = PrintScriptV1TokenType.NUMBER_LITERAL,
        ),
        IdentifierOrKeywordScanner(
            keywordTokenTypesByLexeme = configuration.keywordTokenTypesByLexeme,
            identifierTokenType = PrintScriptV1TokenType.IDENTIFIER,
        ),
        SymbolScanner(
            tokenTypeByLexeme = configuration.symbolTokenTypesByLexeme,
        ),
    )
}
