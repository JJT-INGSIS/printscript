package printscript.lexer

import printscript.lexer.internal.ReaderCharacterCursor
import printscript.lexer.internal.ScanningTokenSource
import printscript.lexer.internal.printScriptV1FixedTokens
import printscript.lexer.internal.scanner.IdentifierOrKeywordScanner
import printscript.lexer.internal.scanner.NumberLiteralScanner
import printscript.lexer.internal.scanner.StringLiteralScanner
import printscript.lexer.internal.scanner.SymbolScanner
import printscript.lexer.internal.scanner.TokenScanner
import printscript.token.LexicalError
import printscript.token.Token
import printscript.token.TokenReadResult
import printscript.token.TokenSource
import java.io.Reader
import java.io.StringReader
import kotlin.test.assertIs

private const val END_OF_INPUT = -1

internal fun cursorFor(input: String): ReaderCharacterCursor {
    return ReaderCharacterCursor(StringReader(input))
}

internal fun scanningTokenSourceFor(input: String): TokenSource {
    val scanners: List<TokenScanner> = listOf(
        StringLiteralScanner(),
        NumberLiteralScanner(),
        IdentifierOrKeywordScanner(printScriptV1FixedTokens),
        SymbolScanner(printScriptV1FixedTokens),
    )

    return ScanningTokenSource(
        cursor = cursorFor(input),
        scanners = scanners,
    )
}

internal fun TokenReadResult.assertSuccessToken(): Token {
    return assertIs<TokenReadResult.Success>(this).token
}

internal inline fun <reified T : LexicalError>
        TokenReadResult.assertLexicalError(): T {
    val failure = assertIs<TokenReadResult.Failure>(this)

    return assertIs<T>(failure.error)
}

internal class TrackingReader(
    private val content: String,
) : Reader() {

    private var currentIndex: Int = 0

    var readCalls: Int = 0
        private set

    var wasClosed: Boolean = false
        private set

    override fun read(
        target: CharArray,
        offset: Int,
        length: Int,
    ): Int {
        readCalls++

        if (length == 0) {
            return 0
        }

        if (currentIndex >= content.length) {
            return END_OF_INPUT
        }

        val charactersToRead = minOf(
            length,
            content.length - currentIndex,
        )

        repeat(charactersToRead) { relativeIndex ->
            target[offset + relativeIndex] =
                content[currentIndex + relativeIndex]
        }

        currentIndex += charactersToRead

        return charactersToRead
    }

    override fun close() {
        wasClosed = true
    }
}