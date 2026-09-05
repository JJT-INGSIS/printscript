package printscript.v1.lexer

import printscript.lexer.Lexer
import printscript.lexer.scanning.TokenScanner

public object PrintScriptV11FormattingLexerFactory {

    @JvmStatic
    @JvmOverloads
    public fun create(
        configuration: PrintScriptV1LexerConfiguration =
            PrintScriptV11LexerFactory.defaultConfiguration(),
        additionalScanners: List<TokenScanner> = emptyList(),
    ): Lexer {
        return PrintScriptV1FormattingLexerFactory.create(
            configuration = configuration,
            additionalScanners = additionalScanners,
        )
    }
}
