package printscript.lexer

import printscript.lexer.internal.ScanningLexer
import printscript.lexer.scanning.IgnoredCharacterPolicy
import printscript.lexer.scanning.TokenScanner
import printscript.token.TokenType

public object LexerFactory {

    public fun create(
        tokenScanners: List<TokenScanner>,
        ignoredCharacterPolicy: IgnoredCharacterPolicy,
        endOfInputTokenType: TokenType,
    ): Lexer {
        return ScanningLexer(
            tokenScanners = tokenScanners,
            ignoredCharacterPolicy = ignoredCharacterPolicy,
            endOfInputTokenType = endOfInputTokenType,
        )
    }
}
