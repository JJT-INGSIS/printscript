package printscript.v1.lexer

import printscript.lexer.scanning.ScannerCharacterReadResult
import printscript.lexer.scanning.ScannerCursor
import printscript.lexer.scanning.TokenScanResult
import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan
import printscript.source.SourceChunk
import printscript.source.SourceChunkReadResult
import printscript.source.SourceReader
import printscript.source.SourceReaderFactory
import printscript.token.LexicalError
import printscript.token.Token
import printscript.token.TokenReadResult
import printscript.token.TokenSource
import printscript.token.TokenType
import kotlin.test.assertEquals
import kotlin.test.assertIs

private const val INITIAL_CHUNK_INDEX = 0
private const val CHUNK_INDEX_INCREMENT = 1
private const val TOKEN_INDEX_INCREMENT = 1

internal data class ExpectedToken(
    val tokenType: TokenType,
    val lexeme: String,
)

internal fun sourceReaderFor(sourceText: String): SourceReader {
    return SourceReaderFactory.fromString(sourceText)
}

internal fun sourceReaderForChunks(vararg chunks: String): SourceReader {
    return ChunkListSourceReader(
        chunks = chunks.toList(),
    )
}

internal fun cursorFor(sourceText: String): ScannerCursor {
    return TestScannerCursor.initial(sourceText)
}

internal fun TokenScanResult.assertSuccessToken(): Token {
    return assertIs<TokenScanResult.Success>(this).token
}

internal inline fun <reified T : LexicalError> TokenScanResult.assertLexicalError(): T {
    val failure = assertIs<TokenScanResult.Failure>(this)

    return assertIs<T>(failure.error)
}

internal inline fun <reified T : LexicalError> TokenReadResult.assertLexicalError(): T {
    val failure = assertIs<TokenReadResult.Failure>(this)

    return assertIs<T>(failure.error)
}

internal fun TokenSource.assertNextToken(expectedToken: ExpectedToken): TokenReadResult.Success {
    val result = assertIs<TokenReadResult.Success>(nextToken())

    assertEquals(
        expected = expectedToken.tokenType,
        actual = result.token.type,
    )

    assertEquals(
        expected = expectedToken.lexeme,
        actual = result.token.lexeme,
    )

    return result
}

internal tailrec fun TokenSource.assertProducesTokenSequence(
    expectedTokens: List<ExpectedToken>,
    currentTokenIndex: Int = 0,
) {
    if (currentTokenIndex >= expectedTokens.size) {
        return
    }

    val result = assertNextToken(
        expectedToken = expectedTokens[currentTokenIndex],
    )

    result.remainingSource.assertProducesTokenSequence(
        expectedTokens = expectedTokens,
        currentTokenIndex =
        currentTokenIndex +
            TOKEN_INDEX_INCREMENT,
    )
}

internal fun ScannerCursor.assertNextCharacter(expectedCharacter: Char) {
    val result = assertIs<ScannerCharacterReadResult.Success>(peek())

    assertEquals(
        expected = expectedCharacter,
        actual = result.character,
    )
}

internal fun ScannerCursor.assertEndOfInput() {
    assertIs<ScannerCharacterReadResult.EndOfInput>(peek())
}

internal fun assertInitialSingleLineSpan(actualSpan: SourceSpan, consumedCharacterCount: Int) {
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

internal data object FailingSourceReader : SourceReader {

    override fun readChunk(): SourceChunkReadResult {
        error("Source must not be read")
    }
}

private data class ChunkListSourceReader(
    private val chunks: List<String>,
    private val currentChunkIndex: Int = INITIAL_CHUNK_INDEX,
) : SourceReader {

    override fun readChunk(): SourceChunkReadResult {
        if (currentChunkIndex >= chunks.size) {
            return SourceChunkReadResult.EndOfInput
        }

        return SourceChunkReadResult.Success(
            chunk = SourceChunk(
                content = chunks[currentChunkIndex],
            ),
            remainingReader = copy(
                currentChunkIndex =
                currentChunkIndex +
                    CHUNK_INDEX_INCREMENT,
            ),
        )
    }
}

private data class TestScannerCursor(
    private val sourceText: String,
    private val currentIndex: Int,
    override val position: SourcePosition,
    private val previousCharacterWasCarriageReturn: Boolean,
) : ScannerCursor {

    override fun peek(): ScannerCharacterReadResult {
        if (currentIndex >= sourceText.length) {
            return ScannerCharacterReadResult.EndOfInput(
                resultingCursor = this,
            )
        }

        return ScannerCharacterReadResult.Success(
            character = sourceText[currentIndex],
            resultingCursor = this,
        )
    }

    override fun advance(): ScannerCharacterReadResult {
        val currentCharacter = sourceText.getOrNull(currentIndex)
            ?: return ScannerCharacterReadResult.EndOfInput(
                resultingCursor = this,
            )

        return ScannerCharacterReadResult.Success(
            character = currentCharacter,
            resultingCursor = after(currentCharacter),
        )
    }

    private fun after(character: Char): TestScannerCursor {
        return copy(
            currentIndex = currentIndex + CHUNK_INDEX_INCREMENT,
            position = positionAfter(character),
            previousCharacterWasCarriageReturn = character == CARRIAGE_RETURN,
        )
    }

    private fun positionAfter(character: Char): SourcePosition {
        return when (character) {
            CARRIAGE_RETURN -> position.nextLine()
            LINE_FEED ->
                if (previousCharacterWasCarriageReturn) {
                    position.nextOffset()
                } else {
                    position.nextLine()
                }

            else -> position.nextColumn()
        }
    }

    companion object {
        const val CARRIAGE_RETURN = '\r'
        const val LINE_FEED = '\n'

        fun initial(sourceText: String): TestScannerCursor {
            return TestScannerCursor(
                sourceText = sourceText,
                currentIndex = INITIAL_CHUNK_INDEX,
                position = SourcePosition.initial(),
                previousCharacterWasCarriageReturn = false,
            )
        }
    }
}
