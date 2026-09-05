package printscript.v1.lexer

import printscript.lexer.Lexer
import printscript.lexer.scanning.TokenScanner
import printscript.v1.lexer.internal.PrintScriptV1IgnoredCharacterPolicy
import printscript.v1.lexer.internal.printScriptV11KeywordTokenTypesByLexeme
import printscript.v1.lexer.internal.printScriptV11SymbolTokenTypesByLexeme
import printscript.v1.lexer.internal.printScriptV1StringQuoteDelimiters

public object PrintScriptV11LexerFactory {

    @JvmStatic
    public fun defaultConfiguration(): PrintScriptV1LexerConfiguration {
        return PrintScriptV1LexerConfiguration(
            keywordTokenTypesByLexeme = printScriptV11KeywordTokenTypesByLexeme,
            symbolTokenTypesByLexeme = printScriptV11SymbolTokenTypesByLexeme,
            stringQuoteDelimiters = printScriptV1StringQuoteDelimiters,
            ignoredCharacterPolicy = PrintScriptV1IgnoredCharacterPolicy,
        )
    }

    @JvmStatic
    @JvmOverloads
    public fun create(
        configuration: PrintScriptV1LexerConfiguration = defaultConfiguration(),
        additionalScanners: List<TokenScanner> = emptyList(),
    ): Lexer {
        return PrintScriptV1LexerFactory.create(
            configuration = configuration,
            additionalScanners = additionalScanners,
        )
    }
}
