package printscript.lexer

import printscript.lexer.internal.ScanningLexer
import printscript.lexer.scanning.TokenScanner
import printscript.token.TokenType

public object LexerFactory {

    public fun create(tokenScanners: List<TokenScanner>, endOfInputTokenType: TokenType): Lexer {
        return ScanningLexer(
            tokenScanners = tokenScanners,
            endOfInputTokenType = endOfInputTokenType,
        )
    }
}
