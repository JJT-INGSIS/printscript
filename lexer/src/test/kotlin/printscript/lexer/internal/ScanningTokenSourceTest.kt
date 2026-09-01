package printscript.lexer.internal

import printscript.lexer.ExpectedToken
import printscript.lexer.SourceReadFailureReader
import printscript.lexer.SourceReadingError
import printscript.lexer.TestSourceReadError
import printscript.lexer.TestTokenType
import printscript.lexer.TestWordScanner
import printscript.lexer.assertLexicalError
import printscript.lexer.assertNextToken
import printscript.lexer.assertTokenReadError
import printscript.lexer.cursorFor
import printscript.lexer.cursorForChunksThenFailure
import printscript.lexer.internal.scanner.TokenScannerDispatcher
import printscript.lexer.scanning.IgnoredCharacterPolicy
import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan
import printscript.token.LexicalError
import printscript.token.TokenSource
import kotlin.test.Test
import kotlin.test.assertEquals

class ScanningTokenSourceTest {

    @Test
    fun `empty input produces EOF at initial position`() {
        val tokenSource = createTokenSourceFor("")

        val eofResult = tokenSource.assertNextToken(
            ExpectedToken(
                tokenType = TestTokenType.END_OF_INPUT,
                lexeme = "",
            ),
        )

        assertEquals(
            expected = SourceSpan(
                start = SourcePosition(1, 1, 0),
                end = SourcePosition(1, 1, 0),
            ),
            actual = eofResult.token.span,
        )
    }

    @Test
    fun `EOF remains stable across repeated requests`() {
        val tokenSource = createTokenSourceFor("")

        val expectedEof = ExpectedToken(
            tokenType = TestTokenType.END_OF_INPUT,
            lexeme = "",
        )

        val firstEofResult =
            tokenSource.assertNextToken(expectedEof)

        val secondEofResult =
            firstEofResult.remainingSource
                .assertNextToken(expectedEof)

        assertEquals(
            expected = firstEofResult.token,
            actual = secondEofResult.token,
        )
    }

    @Test
    fun `whitespace-only input produces EOF after consumed whitespace`() {
        val tokenSource = createTokenSourceFor(" \n")

        val eofResult = tokenSource.assertNextToken(
            ExpectedToken(
                tokenType = TestTokenType.END_OF_INPUT,
                lexeme = "",
            ),
        )

        assertEquals(
            expected = SourceSpan(
                start = SourcePosition(2, 1, 2),
                end = SourcePosition(2, 1, 2),
            ),
            actual = eofResult.token.span,
        )
    }

    @Test
    fun `leading whitespace is consumed before each token`() {
        val tokenSource = createTokenSourceFor(
            " token \n next",
        )

        val firstTokenResult = tokenSource.assertNextToken(
            ExpectedToken(
                tokenType = TestTokenType.FIRST_WORD,
                lexeme = "token",
            ),
        )

        assertEquals(
            expected = SourceSpan(
                start = SourcePosition(1, 2, 1),
                end = SourcePosition(1, 7, 6),
            ),
            actual = firstTokenResult.token.span,
        )

        val secondTokenResult =
            firstTokenResult.remainingSource.assertNextToken(
                ExpectedToken(
                    tokenType = TestTokenType.FIRST_WORD,
                    lexeme = "next",
                ),
            )

        assertEquals(
            expected = SourceSpan(
                start = SourcePosition(2, 2, 9),
                end = SourcePosition(2, 6, 13),
            ),
            actual = secondTokenResult.token.span,
        )
    }

    @Test
    fun `lexical failure does not prevent reading following token`() {
        val tokenSource = createTokenSourceFor("@token")

        val failureResult = tokenSource.nextToken()
        val lexicalError =
            failureResult.assertLexicalError<LexicalError.UnexpectedCharacter>()

        assertEquals('@', lexicalError.character)

        assertEquals(
            expected = SourceSpan(
                start = SourcePosition(1, 1, 0),
                end = SourcePosition(1, 2, 1),
            ),
            actual = lexicalError.span,
        )

        val followingTokenResult =
            failureResult.remainingSource.assertNextToken(
                ExpectedToken(
                    tokenType = TestTokenType.FIRST_WORD,
                    lexeme = "token",
                ),
            )

        assertEquals(
            expected = SourceSpan(
                start = SourcePosition(1, 2, 1),
                end = SourcePosition(1, 7, 6),
            ),
            actual = followingTokenResult.token.span,
        )
    }

    @Test
    fun `source reading failure is returned as a token reading failure`() {
        val sourceError = TestSourceReadError("temporary failure")
        val tokenSource = createTokenSource(
            cursor = CharacterCursor.initial(SourceReadFailureReader(sourceError)),
        )

        val error = tokenSource.nextToken().assertTokenReadError<SourceReadingError>()

        assertEquals(expected = sourceError, actual = error.sourceError)
        assertEquals(
            expected = SourceSpan(
                start = SourcePosition.initial(),
                end = SourcePosition.initial(),
            ),
            actual = error.span,
        )
    }

    @Test
    fun `source reading failure inside a token preserves its source position`() {
        val sourceError = TestSourceReadError("temporary failure")
        val tokenSource = createTokenSource(
            cursor = cursorForChunksThenFailure(sourceError, "word"),
        )

        val error = tokenSource.nextToken().assertTokenReadError<SourceReadingError>()

        assertEquals(expected = sourceError, actual = error.sourceError)
        assertEquals(
            expected = SourceSpan(
                start = SourcePosition(line = 1, column = 5, offset = 4L),
                end = SourcePosition(line = 1, column = 5, offset = 4L),
            ),
            actual = error.span,
        )
    }

    private fun createTokenSourceFor(sourceText: String): TokenSource {
        return createTokenSource(cursorFor(sourceText))
    }

    private fun createTokenSource(cursor: CharacterCursor): TokenSource {
        val tokenScannerDispatcher = TokenScannerDispatcher(
            scanners = listOf(
                TestWordScanner(
                    tokenType = TestTokenType.FIRST_WORD,
                ),
            ),
        )

        return ScanningTokenSource(
            characterCursor = cursor,
            tokenScannerDispatcher = tokenScannerDispatcher,
            ignoredCharacterPolicy =
            IgnoredCharacterPolicy { character ->
                character.isWhitespace()
            },
            endOfInputTokenType = TestTokenType.END_OF_INPUT,
        )
    }
}
