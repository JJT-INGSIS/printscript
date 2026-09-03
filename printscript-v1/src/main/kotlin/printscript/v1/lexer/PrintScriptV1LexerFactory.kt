package printscript.v1.lexer

import printscript.lexer.Lexer
import printscript.lexer.LexerFactory
import printscript.lexer.scanning.TokenScanner
import printscript.v1.lexer.internal.PrintScriptV1IgnoredCharacterPolicy
import printscript.v1.lexer.internal.printScriptV1KeywordTokenTypesByLexeme
import printscript.v1.lexer.internal.printScriptV1StringQuoteDelimiters
import printscript.v1.lexer.internal.printScriptV1SymbolTokenTypesByLexeme
import printscript.v1.lexer.internal.printScriptV1TokenScanners
import printscript.v1.token.PrintScriptV1TokenType

public object PrintScriptV1LexerFactory {

    @JvmStatic
    public fun defaultConfiguration(): PrintScriptV1LexerConfiguration {
        return PrintScriptV1LexerConfiguration(
            keywordTokenTypesByLexeme = printScriptV1KeywordTokenTypesByLexeme,
            symbolTokenTypesByLexeme = printScriptV1SymbolTokenTypesByLexeme,
            stringQuoteDelimiters = printScriptV1StringQuoteDelimiters,
            ignoredCharacterPolicy = PrintScriptV1IgnoredCharacterPolicy,
        )
    }

    /**
     * Creates the V1 lexer. Additional scanners are evaluated before the
     * scanners included by V1, allowing callers to extend or override them.
     */
    @JvmStatic
    @JvmOverloads
    public fun create(
        configuration: PrintScriptV1LexerConfiguration = defaultConfiguration(),
        additionalScanners: List<TokenScanner> = emptyList(),
    ): Lexer {
        return LexerFactory.create(
            tokenScanners =
            additionalScanners +
                printScriptV1TokenScanners(configuration),
            ignoredCharacterPolicy = configuration.ignoredCharacterPolicy,
            endOfInputTokenType = PrintScriptV1TokenType.EOF,
        )
    }
}
