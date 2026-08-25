package printscript.lexer

import printscript.lexer.internal.CharacterCursor
import printscript.lexer.internal.CharacterReadResult
import printscript.lexer.internal.scanner.TokenScanResult
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

internal fun cursorFor(sourceText: String): CharacterCursor {
    return CharacterCursor.initial(
        sourceReader = sourceReaderFor(sourceText),
    )
}

internal fun cursorForChunks(vararg chunks: String): CharacterCursor {
    return CharacterCursor.initial(
        sourceReader = sourceReaderForChunks(*chunks),
    )
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

internal fun CharacterCursor.assertNextCharacter(expectedCharacter: Char) {
    val result = assertIs<CharacterReadResult.Success>(peek())

    assertEquals(
        expected = expectedCharacter,
        actual = result.character,
    )
}

internal fun CharacterCursor.assertEndOfInput() {
    assertIs<CharacterReadResult.EndOfInput>(peek())
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
