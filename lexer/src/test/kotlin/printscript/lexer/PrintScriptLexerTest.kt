package printscript.lexer

import printscript.lexer.internal.scanner.IdentifierOrKeywordScanner
import printscript.token.LexicalError
import printscript.token.TokenType
import java.io.StringReader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PrintScriptLexerTest {

    private val lexer: Lexer =
        PrintScriptLexerFactory.createV1()

    @Test
    fun `tokenizes complete PrintScript V1 program in token order`() {
        val sourceCode = """
            let age: number = 12.5;
            let name: string = 'Joe';
            println(name + " Doe");
        """.trimIndent()

        val tokenSource = lexer.tokenize(
            StringReader(sourceCode),
        )

        val expectedTokens = listOf(
            ExpectedToken(TokenType.LET, "let"),
            ExpectedToken(TokenType.IDENTIFIER, "age"),
            ExpectedToken(TokenType.COLON, ":"),
            ExpectedToken(TokenType.NUMBER_TYPE, "number"),
            ExpectedToken(TokenType.ASSIGN, "="),
            ExpectedToken(TokenType.NUMBER_LITERAL, "12.5"),
            ExpectedToken(TokenType.SEMICOLON, ";"),

            ExpectedToken(TokenType.LET, "let"),
            ExpectedToken(TokenType.IDENTIFIER, "name"),
            ExpectedToken(TokenType.COLON, ":"),
            ExpectedToken(TokenType.STRING_TYPE, "string"),
            ExpectedToken(TokenType.ASSIGN, "="),
            ExpectedToken(TokenType.STRING_LITERAL, "'Joe'"),
            ExpectedToken(TokenType.SEMICOLON, ";"),

            ExpectedToken(TokenType.PRINTLN, "println"),
            ExpectedToken(TokenType.LEFT_PAREN, "("),
            ExpectedToken(TokenType.IDENTIFIER, "name"),
            ExpectedToken(TokenType.PLUS, "+"),
            ExpectedToken(TokenType.STRING_LITERAL, "\" Doe\""),
            ExpectedToken(TokenType.RIGHT_PAREN, ")"),
            ExpectedToken(TokenType.SEMICOLON, ";"),

            ExpectedToken(TokenType.EOF, ""),
        )

        tokenSource.assertProducesTokenSequence(expectedTokens)
    }

    @Test
    fun `uses injected scanner configuration without V1 defaults`() {
        val configurableLexer: Lexer = PrintScriptLexer(
            tokenScanners = listOf(
                IdentifierOrKeywordScanner(
                    fixedTokenTypesByLexeme = mapOf(
                        "var" to TokenType.LET,
                    ),
                ),
            ),
        )

        val tokenSource = configurableLexer.tokenize(
            StringReader("var let"),
        )

        tokenSource.assertProducesTokenSequence(
            listOf(
                ExpectedToken(
                    tokenType = TokenType.LET,
                    lexeme = "var",
                ),
                ExpectedToken(
                    tokenType = TokenType.IDENTIFIER,
                    lexeme = "let",
                ),
                ExpectedToken(
                    tokenType = TokenType.EOF,
                    lexeme = "",
                ),
            ),
        )
    }

    @Test
    fun `tokenize does not read input until first token is requested`() {
        val inputReader = TrackingReader("let")

        val tokenSource = lexer.tokenize(inputReader)

        assertEquals(
            expected = 0,
            actual = inputReader.readCalls,
        )

        tokenSource.assertNextToken(
            ExpectedToken(
                tokenType = TokenType.LET,
                lexeme = "let",
            ),
        )

        assertTrue(inputReader.readCalls > 0)
    }

    @Test
    fun `lexer leaves input reader open`() {
        val inputReader = TrackingReader("let")
        val tokenSource = lexer.tokenize(inputReader)

        tokenSource.assertProducesTokenSequence(
            listOf(
                ExpectedToken(TokenType.LET, "let"),
                ExpectedToken(TokenType.EOF, ""),
            ),
        )

        assertFalse(inputReader.wasClosed)
    }

    @Test
    fun `one lexer instance creates independent token sources`() {
        val firstTokenSource = lexer.tokenize(
            StringReader("let first"),
        )

        val secondTokenSource = lexer.tokenize(
            StringReader("println second"),
        )

        firstTokenSource.assertNextToken(
            ExpectedToken(TokenType.LET, "let"),
        )

        secondTokenSource.assertNextToken(
            ExpectedToken(TokenType.PRINTLN, "println"),
        )

        firstTokenSource.assertNextToken(
            ExpectedToken(TokenType.IDENTIFIER, "first"),
        )

        secondTokenSource.assertNextToken(
            ExpectedToken(TokenType.IDENTIFIER, "second"),
        )
    }

    @Test
    fun `decimal separator cannot start number literal`() {
        val tokenSource = lexer.tokenize(
            StringReader(".5"),
        )

        val lexicalError = tokenSource.nextToken()
            .assertLexicalError<LexicalError.UnexpectedCharacter>()

        assertEquals(
            expected = '.',
            actual = lexicalError.character,
        )

        tokenSource.assertNextToken(
            ExpectedToken(
                tokenType = TokenType.NUMBER_LITERAL,
                lexeme = "5",
            ),
        )
    }
}