package printscript.v1.lexer

import printscript.lexer.Lexer
import printscript.source.SourceReaderCreationResult
import printscript.source.SourceReaderFactory
import printscript.token.LexicalError
import printscript.token.TokenType
import printscript.v1.lexer.internal.scanner.IdentifierOrKeywordScanner
import printscript.v1.token.PrintScriptV1TokenType
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class PrintScriptV1LexerTest {

    private val lexer: Lexer =
        PrintScriptV1LexerFactory.create()

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
    fun `uses configured keyword lexemes`() {
        val defaultConfiguration =
            PrintScriptV1LexerFactory.defaultConfiguration()
        val configuredLexer =
            PrintScriptV1LexerFactory.create(
                configuration =
                PrintScriptV1LexerConfiguration(
                    keywordTokenTypesByLexeme =
                    defaultConfiguration.keywordTokenTypesByLexeme
                        .minus("let")
                        .plus("var" to PrintScriptV1TokenType.LET),
                    symbolTokenTypesByLexeme =
                    defaultConfiguration.symbolTokenTypesByLexeme,
                    stringQuoteDelimiters =
                    defaultConfiguration.stringQuoteDelimiters,
                    ignoredCharacterPolicy =
                    defaultConfiguration.ignoredCharacterPolicy,
                ),
            )

        val tokenSource = configuredLexer.tokenize(
            sourceReaderFor("var let"),
        )

        tokenSource.assertProducesTokenSequence(
            listOf(
                ExpectedToken(PrintScriptV1TokenType.LET, "var"),
                ExpectedToken(PrintScriptV1TokenType.IDENTIFIER, "let"),
                ExpectedToken(PrintScriptV1TokenType.EOF, ""),
            ),
        )
    }

    @Test
    fun `additional scanners have priority over V1 scanners`() {
        val lexerWithExternalScanner =
            PrintScriptV1LexerFactory.create(
                additionalScanners = listOf(
                    IdentifierOrKeywordScanner(
                        keywordTokenTypesByLexeme = mapOf(
                            "let" to CustomTokenType.KEYWORD,
                        ),
                        identifierTokenType = CustomTokenType.IDENTIFIER,
                    ),
                ),
            )

        val tokenSource = lexerWithExternalScanner.tokenize(
            sourceReaderFor("let"),
        )

        tokenSource.assertProducesTokenSequence(
            listOf(
                ExpectedToken(CustomTokenType.KEYWORD, "let"),
                ExpectedToken(PrintScriptV1TokenType.EOF, ""),
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
                "ln(value);\nlet text: string = \"hel",
                "lo\";",
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
                ExpectedToken(PrintScriptV1TokenType.LET, "let"),
                ExpectedToken(PrintScriptV1TokenType.IDENTIFIER, "text"),
                ExpectedToken(PrintScriptV1TokenType.COLON, ":"),
                ExpectedToken(PrintScriptV1TokenType.STRING_TYPE, "string"),
                ExpectedToken(PrintScriptV1TokenType.ASSIGN, "="),
                ExpectedToken(PrintScriptV1TokenType.STRING_LITERAL, "\"hello\""),
                ExpectedToken(PrintScriptV1TokenType.SEMICOLON, ";"),
                ExpectedToken(PrintScriptV1TokenType.EOF, ""),
            ),
        )
    }

    @Test
    fun `tokenizes a UTF-8 input stream across character buffers`() {
        val creation = SourceReaderFactory.fromInputStream(
            inputStream = ByteArrayInputStream(
                "let café: string = \"sí\";"
                    .toByteArray(StandardCharsets.UTF_8),
            ),
            bufferSizeInCharacters = 1,
        )
        val reader = assertIs<SourceReaderCreationResult.Success>(creation).reader

        lexer.tokenize(reader).assertProducesTokenSequence(
            listOf(
                ExpectedToken(PrintScriptV1TokenType.LET, "let"),
                ExpectedToken(PrintScriptV1TokenType.IDENTIFIER, "café"),
                ExpectedToken(PrintScriptV1TokenType.COLON, ":"),
                ExpectedToken(PrintScriptV1TokenType.STRING_TYPE, "string"),
                ExpectedToken(PrintScriptV1TokenType.ASSIGN, "="),
                ExpectedToken(PrintScriptV1TokenType.STRING_LITERAL, "\"sí\""),
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
    }
}
