package printscript.v1.lexer

import printscript.lexer.Lexer
import printscript.lexer.LexerFactory
import printscript.lexer.scanning.TokenScanner
import printscript.v1.lexer.internal.PreserveEveryCharacterPolicy
import printscript.v1.lexer.internal.printScriptV1TokenScanners
import printscript.v1.lexer.internal.scanner.WhitespaceScanner
import printscript.v1.token.PrintScriptV1TokenType

/**
 * Creates the lossless V1 token stream consumed by formatting tools.
 *
 * The ignored-character policy from [PrintScriptV1LexerConfiguration] is
 * deliberately replaced so that every whitespace character reaches the
 * formatter. The remaining lexical configuration is shared with the normal
 * V1 lexer.
 */
public object PrintScriptV1FormattingLexerFactory {

    @JvmStatic
    @JvmOverloads
    public fun create(
        configuration: PrintScriptV1LexerConfiguration =
            PrintScriptV1LexerFactory.defaultConfiguration(),
        additionalScanners: List<TokenScanner> = emptyList(),
    ): Lexer {
        return LexerFactory.create(
            tokenScanners =
            additionalScanners +
                WhitespaceScanner(PrintScriptV1FormattingTokenType.WHITESPACE) +
                printScriptV1TokenScanners(configuration),
            ignoredCharacterPolicy = PreserveEveryCharacterPolicy,
            endOfInputTokenType = PrintScriptV1TokenType.EOF,
        )
    }
}
