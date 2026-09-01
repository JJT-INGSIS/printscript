package printscript.lexer.scanning

import printscript.token.Token
import printscript.token.TokenReadError

public sealed interface TokenScanResult {

    public val resultingCursor: ScannerCursor

    public data class Success(
        public val token: Token,
        override val resultingCursor: ScannerCursor,
    ) : TokenScanResult

    public data class Failure(
        public val error: TokenReadError,
        override val resultingCursor: ScannerCursor,
    ) : TokenScanResult
}
