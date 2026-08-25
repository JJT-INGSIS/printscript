package printscript.lexer

import printscript.lexer.internal.scanner.IdentifierOrKeywordScanner
import printscript.token.LexicalError
import printscript.token.TokenType
import kotlin.test.Test
import kotlin.test.assertEquals

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
            sourceReaderFor(sourceCode),
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
                    keywordTokenTypesByLexeme = mapOf(
                        "var" to TokenType.LET,
                    ),
                ),
            ),
        )

        val tokenSource = configurableLexer.tokenize(
            sourceReaderFor("var let"),
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
    fun `tokenizes source independently of source chunk boundaries`() {
        val tokenSource = lexer.tokenize(
            sourceReaderForChunks(
                "le",
                "t value",
                ": num",
                "ber = 1",
                ";\nprint",
                "ln(value);",
            ),
        )

        tokenSource.assertProducesTokenSequence(
            listOf(
                ExpectedToken(TokenType.LET, "let"),
                ExpectedToken(TokenType.IDENTIFIER, "value"),
                ExpectedToken(TokenType.COLON, ":"),
                ExpectedToken(TokenType.NUMBER_TYPE, "number"),
                ExpectedToken(TokenType.ASSIGN, "="),
                ExpectedToken(TokenType.NUMBER_LITERAL, "1"),
                ExpectedToken(TokenType.SEMICOLON, ";"),
                ExpectedToken(TokenType.PRINTLN, "println"),
                ExpectedToken(TokenType.LEFT_PAREN, "("),
                ExpectedToken(TokenType.IDENTIFIER, "value"),
                ExpectedToken(TokenType.RIGHT_PAREN, ")"),
                ExpectedToken(TokenType.SEMICOLON, ";"),
                ExpectedToken(TokenType.EOF, ""),
            ),
        )
    }

    @Test
    fun `tokenize does not read input until first token is requested`() {
        lexer.tokenize(FailingSourceReader)
    }

    @Test
    fun `reading token does not modify original token source`() {
        val tokenSource = lexer.tokenize(
            sourceReaderFor("let value"),
        )

        val firstRead = tokenSource.assertNextToken(
            ExpectedToken(TokenType.LET, "let"),
        )

        val repeatedRead = tokenSource.assertNextToken(
            ExpectedToken(TokenType.LET, "let"),
        )

        assertEquals(
            expected = firstRead.token,
            actual = repeatedRead.token,
        )

        firstRead.remainingSource.assertNextToken(
            ExpectedToken(TokenType.IDENTIFIER, "value"),
        )
    }

    @Test
    fun `one lexer instance creates independent token sources`() {
        val firstTokenSource = lexer.tokenize(
            sourceReaderFor("let first"),
        )

        val secondTokenSource = lexer.tokenize(
            sourceReaderFor("println second"),
        )

        val firstResult = firstTokenSource.assertNextToken(
            ExpectedToken(TokenType.LET, "let"),
        )

        val secondResult = secondTokenSource.assertNextToken(
            ExpectedToken(TokenType.PRINTLN, "println"),
        )

        firstResult.remainingSource.assertNextToken(
            ExpectedToken(
                TokenType.IDENTIFIER,
                "first",
            ),
        )

        secondResult.remainingSource.assertNextToken(
            ExpectedToken(
                TokenType.IDENTIFIER,
                "second",
            ),
        )
    }

    @Test
    fun `decimal separator cannot start number literal`() {
        val tokenSource = lexer.tokenize(
            sourceReaderFor(".5"),
        )

        val failureResult = tokenSource.nextToken()
        val lexicalError =
            failureResult.assertLexicalError<LexicalError.UnexpectedCharacter>()

        assertEquals(
            expected = '.',
            actual = lexicalError.character,
        )

        failureResult.remainingSource.assertNextToken(
            ExpectedToken(
                tokenType = TokenType.NUMBER_LITERAL,
                lexeme = "5",
            ),
        )
    }
}
