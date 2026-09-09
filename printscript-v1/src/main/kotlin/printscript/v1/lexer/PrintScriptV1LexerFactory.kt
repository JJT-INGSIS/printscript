package printscript.v1.lexer

import printscript.lexer.Lexer
import printscript.lexer.LexerFactory
import printscript.lexer.scanning.TokenScanner
import printscript.v1.lexer.internal.PreserveEveryCharacterPolicy
import printscript.v1.lexer.internal.printScriptV1KeywordTokenTypesByLexeme
import printscript.v1.lexer.internal.printScriptV1StringQuoteDelimiters
import printscript.v1.lexer.internal.printScriptV1SymbolTokenTypesByCharacter
import printscript.v1.lexer.internal.printScriptV1TokenScanners
import printscript.v1.token.PrintScriptV1TokenType

public object PrintScriptV1LexerFactory {

    @JvmStatic
    public fun defaultConfiguration(): PrintScriptV1LexerConfiguration {
        return PrintScriptV1LexerConfiguration(
            keywordTokenTypesByLexeme = printScriptV1KeywordTokenTypesByLexeme,
            symbolTokenTypesByCharacter = printScriptV1SymbolTokenTypesByCharacter,
            stringQuoteDelimiters = printScriptV1StringQuoteDelimiters,
        )
    }

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
            ignoredCharacterPolicy = PreserveEveryCharacterPolicy,
            endOfInputTokenType = PrintScriptV1TokenType.EOF,
        )
    }
}
