package printscript.lexer

import printscript.lexer.internal.scanner.IdentifierOrKeywordScanner
import printscript.token.LexicalError
import printscript.token.PrintScriptV1TokenType
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
            ExpectedToken(PrintScriptV1TokenType.LET, "let"),
            ExpectedToken(PrintScriptV1TokenType.IDENTIFIER, "age"),
            ExpectedToken(PrintScriptV1TokenType.COLON, ":"),
            ExpectedToken(PrintScriptV1TokenType.NUMBER_TYPE, "number"),
            ExpectedToken(PrintScriptV1TokenType.ASSIGN, "="),
            ExpectedToken(PrintScriptV1TokenType.NUMBER_LITERAL, "12.5"),
            ExpectedToken(PrintScriptV1TokenType.SEMICOLON, ";"),

            ExpectedToken(PrintScriptV1TokenType.LET, "let"),
            ExpectedToken(PrintScriptV1TokenType.IDENTIFIER, "name"),
            ExpectedToken(PrintScriptV1TokenType.COLON, ":"),
            ExpectedToken(PrintScriptV1TokenType.STRING_TYPE, "string"),
            ExpectedToken(PrintScriptV1TokenType.ASSIGN, "="),
            ExpectedToken(PrintScriptV1TokenType.STRING_LITERAL, "'Joe'"),
            ExpectedToken(PrintScriptV1TokenType.SEMICOLON, ";"),

            ExpectedToken(PrintScriptV1TokenType.PRINTLN, "println"),
            ExpectedToken(PrintScriptV1TokenType.LEFT_PAREN, "("),
            ExpectedToken(PrintScriptV1TokenType.IDENTIFIER, "name"),
            ExpectedToken(PrintScriptV1TokenType.PLUS, "+"),
            ExpectedToken(PrintScriptV1TokenType.STRING_LITERAL, "\" Doe\""),
            ExpectedToken(PrintScriptV1TokenType.RIGHT_PAREN, ")"),
            ExpectedToken(PrintScriptV1TokenType.SEMICOLON, ";"),

            ExpectedToken(PrintScriptV1TokenType.EOF, ""),
        )

        tokenSource.assertProducesTokenSequence(expectedTokens)
    }

    @Test
    fun `uses injected scanner configuration without V1 defaults`() {
        val configurableLexer: Lexer = PrintScriptLexer(
            endOfInputTokenType = CustomTokenType.END_OF_INPUT,
            tokenScanners = listOf(
                IdentifierOrKeywordScanner(
                    keywordTokenTypesByLexeme = mapOf(
                        "var" to CustomTokenType.KEYWORD,
                    ),
                    identifierTokenType = CustomTokenType.IDENTIFIER,
                ),
            ),
        )

        val tokenSource = configurableLexer.tokenize(
            sourceReaderFor("var let"),
        )

        tokenSource.assertProducesTokenSequence(
            listOf(
                ExpectedToken(
                    tokenType = CustomTokenType.KEYWORD,
                    lexeme = "var",
                ),
                ExpectedToken(
                    tokenType = CustomTokenType.IDENTIFIER,
                    lexeme = "let",
                ),
                ExpectedToken(
                    tokenType = CustomTokenType.END_OF_INPUT,
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
                ExpectedToken(PrintScriptV1TokenType.LET, "let"),
                ExpectedToken(PrintScriptV1TokenType.IDENTIFIER, "value"),
                ExpectedToken(PrintScriptV1TokenType.COLON, ":"),
                ExpectedToken(PrintScriptV1TokenType.NUMBER_TYPE, "number"),
                ExpectedToken(PrintScriptV1TokenType.ASSIGN, "="),
                ExpectedToken(PrintScriptV1TokenType.NUMBER_LITERAL, "1"),
                ExpectedToken(PrintScriptV1TokenType.SEMICOLON, ";"),
                ExpectedToken(PrintScriptV1TokenType.PRINTLN, "println"),
                ExpectedToken(PrintScriptV1TokenType.LEFT_PAREN, "("),
                ExpectedToken(PrintScriptV1TokenType.IDENTIFIER, "value"),
                ExpectedToken(PrintScriptV1TokenType.RIGHT_PAREN, ")"),
                ExpectedToken(PrintScriptV1TokenType.SEMICOLON, ";"),
                ExpectedToken(PrintScriptV1TokenType.EOF, ""),
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
            ExpectedToken(PrintScriptV1TokenType.LET, "let"),
        )

        val repeatedRead = tokenSource.assertNextToken(
            ExpectedToken(PrintScriptV1TokenType.LET, "let"),
        )

        assertEquals(
            expected = firstRead.token,
            actual = repeatedRead.token,
        )

        firstRead.remainingSource.assertNextToken(
            ExpectedToken(PrintScriptV1TokenType.IDENTIFIER, "value"),
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
            ExpectedToken(PrintScriptV1TokenType.LET, "let"),
        )

        val secondResult = secondTokenSource.assertNextToken(
            ExpectedToken(PrintScriptV1TokenType.PRINTLN, "println"),
        )

        firstResult.remainingSource.assertNextToken(
            ExpectedToken(
                PrintScriptV1TokenType.IDENTIFIER,
                "first",
            ),
        )

        secondResult.remainingSource.assertNextToken(
            ExpectedToken(
                PrintScriptV1TokenType.IDENTIFIER,
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
                tokenType = PrintScriptV1TokenType.NUMBER_LITERAL,
                lexeme = "5",
            ),
        )
    }

    private enum class CustomTokenType : TokenType {
        KEYWORD,
        IDENTIFIER,
        END_OF_INPUT,
    }
}
