package printscript.lexer.scanning

import printscript.token.LexicalError
import printscript.token.Token

public sealed interface TokenScanResult {

    public val resultingCursor: ScannerCursor

    public data class Success(
        public val token: Token,
        override val resultingCursor: ScannerCursor,
    ) : TokenScanResult

    public data class Failure(
        public val error: LexicalError,
        override val resultingCursor: ScannerCursor,
    ) : TokenScanResult
}
