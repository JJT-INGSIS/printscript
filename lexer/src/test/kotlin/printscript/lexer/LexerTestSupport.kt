package printscript.lexer

import printscript.lexer.internal.ReaderCharacterCursor
import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan
import printscript.token.LexicalError
import printscript.token.Token
import printscript.token.TokenReadResult
import printscript.token.TokenSource
import printscript.token.TokenType
import java.io.Reader
import java.io.StringReader
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal data class ExpectedToken(
    val tokenType: TokenType,
    val lexeme: String,
)

internal fun cursorFor(
    sourceText: String,
): ReaderCharacterCursor {
    return ReaderCharacterCursor(
        inputReader = StringReader(sourceText),
    )
}

internal fun TokenReadResult.assertSuccessToken(): Token {
    return assertIs<TokenReadResult.Success>(this).token
}

internal inline fun <reified T : LexicalError> TokenReadResult.assertLexicalError(): T {
    val failure = assertIs<TokenReadResult.Failure>(this)

    return assertIs<T>(failure.error)
}

internal fun TokenSource.assertNextToken(
    expectedToken: ExpectedToken,
): Token {
    val actualToken = nextToken().assertSuccessToken()

    assertEquals(
        expected = expectedToken.tokenType,
        actual = actualToken.type,
    )

    assertEquals(
        expected = expectedToken.lexeme,
        actual = actualToken.lexeme,
    )

    return actualToken
}

internal fun TokenSource.assertProducesTokenSequence(
    expectedTokens: List<ExpectedToken>,
) {
    for (expectedToken in expectedTokens) {
        assertNextToken(expectedToken)
    }
}

internal fun assertInitialSingleLineSpan(
    actualSpan: SourceSpan,
    consumedCharacterCount: Int,
) {
    val expectedSpan = SourceSpan(
        start = SourcePosition(
            line = 1,
            column = 1,
            offset = 0,
        ),
        end = SourcePosition(
            line = 1,
            column = consumedCharacterCount + 1,
            offset = consumedCharacterCount.toLong(),
        ),
    )

    assertEquals(
        expected = expectedSpan,
        actual = actualSpan,
    )
}

internal class TrackingReader(
    sourceText: String,
) : Reader() {

    private val delegateReader = StringReader(sourceText)

    var readCalls: Int = 0
        private set

    var wasClosed: Boolean = false
        private set

    override fun read(
        destination: CharArray,
        destinationOffset: Int,
        requestedCharacterCount: Int,
    ): Int {
        readCalls++

        return delegateReader.read(
            destination,
            destinationOffset,
            requestedCharacterCount,
        )
    }

    override fun close() {
        wasClosed = true
        delegateReader.close()
    }
}