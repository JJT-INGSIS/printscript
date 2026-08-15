package printscript.lexer.internal

import printscript.lexer.ExpectedToken
import printscript.lexer.assertLexicalError
import printscript.lexer.assertNextToken
import printscript.lexer.cursorFor
import printscript.lexer.internal.scanner.IdentifierOrKeywordScanner
import printscript.lexer.internal.scanner.TokenScannerDispatcher
import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan
import printscript.token.LexicalError
import printscript.token.TokenSource
import printscript.token.TokenType
import kotlin.test.Test
import kotlin.test.assertEquals

class ScanningTokenSourceTest {

    @Test
    fun `empty input produces EOF at initial position`() {
        val tokenSource = createTokenSourceFor("")

        val eofToken = tokenSource.assertNextToken(
            ExpectedToken(
                tokenType = TokenType.EOF,
                lexeme = "",
            ),
        )

        assertEquals(
            expected = SourceSpan(
                start = SourcePosition(1, 1, 0),
                end = SourcePosition(1, 1, 0),
            ),
            actual = eofToken.span,
        )
    }

    @Test
    fun `EOF remains stable across repeated requests`() {
        val tokenSource = createTokenSourceFor("")

        val expectedEof = ExpectedToken(
            tokenType = TokenType.EOF,
            lexeme = "",
        )

        val firstEof = tokenSource.assertNextToken(expectedEof)
        val secondEof = tokenSource.assertNextToken(expectedEof)

        assertEquals(firstEof, secondEof)
    }

    @Test
    fun `whitespace-only input produces EOF after consumed whitespace`() {
        val tokenSource = createTokenSourceFor(" \n")

        val eofToken = tokenSource.assertNextToken(
            ExpectedToken(
                tokenType = TokenType.EOF,
                lexeme = "",
            ),
        )

        assertEquals(
            expected = SourceSpan(
                start = SourcePosition(2, 1, 2),
                end = SourcePosition(2, 1, 2),
            ),
            actual = eofToken.span,
        )
    }

    @Test
    fun `leading whitespace is consumed before each token`() {
        val tokenSource = createTokenSourceFor(
            " token \n next",
        )

        val firstToken = tokenSource.assertNextToken(
            ExpectedToken(
                tokenType = TokenType.IDENTIFIER,
                lexeme = "token",
            ),
        )

        assertEquals(
            expected = SourceSpan(
                start = SourcePosition(1, 2, 1),
                end = SourcePosition(1, 7, 6),
            ),
            actual = firstToken.span,
        )

        val secondToken = tokenSource.assertNextToken(
            ExpectedToken(
                tokenType = TokenType.IDENTIFIER,
                lexeme = "next",
            ),
        )

        assertEquals(
            expected = SourceSpan(
                start = SourcePosition(2, 2, 9),
                end = SourcePosition(2, 6, 13),
            ),
            actual = secondToken.span,
        )
    }

    @Test
    fun `lexical failure does not prevent reading following token`() {
        val tokenSource = createTokenSourceFor("@token")

        val lexicalError = tokenSource.nextToken()
            .assertLexicalError<LexicalError.UnexpectedCharacter>()

        assertEquals('@', lexicalError.character)

        assertEquals(
            expected = SourceSpan(
                start = SourcePosition(1, 1, 0),
                end = SourcePosition(1, 2, 1),
            ),
            actual = lexicalError.span,
        )

        val followingToken = tokenSource.assertNextToken(
            ExpectedToken(
                tokenType = TokenType.IDENTIFIER,
                lexeme = "token",
            ),
        )

        assertEquals(
            expected = SourceSpan(
                start = SourcePosition(1, 2, 1),
                end = SourcePosition(1, 7, 6),
            ),
            actual = followingToken.span,
        )
    }

    private fun createTokenSourceFor(
        sourceText: String,
    ): TokenSource {
        val tokenScannerDispatcher = TokenScannerDispatcher(
            scanners = listOf(
                IdentifierOrKeywordScanner(emptyMap()),
            ),
        )

        return ScanningTokenSource(
            characterCursor = cursorFor(sourceText),
            tokenScannerDispatcher = tokenScannerDispatcher,
        )
    }
}