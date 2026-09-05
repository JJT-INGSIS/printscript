package printscript.v1.lexer

import printscript.lexer.Lexer
import printscript.v1.token.PrintScriptV1TokenType
import kotlin.test.Test

class PrintScriptV11LexerTest {

    private val lexer: Lexer = PrintScriptV11LexerFactory.create()

    @Test
    fun `tokenizes every addition introduced by PrintScript V1_1`() {
        val sourceCode = """
            const active: boolean = true;
            if (active) {
                let name: string = readInput("Name:");
                let club: string = readEnv("BEST_FOOTBALL_CLUB");
            } else {
                println(false);
            }
        """.trimIndent()

        lexer.tokenize(sourceReaderFor(sourceCode)).assertProducesTokenSequence(
            listOf(
                ExpectedToken(PrintScriptV1TokenType.CONST, "const"),
                ExpectedToken(PrintScriptV1TokenType.IDENTIFIER, "active"),
                ExpectedToken(PrintScriptV1TokenType.COLON, ":"),
                ExpectedToken(PrintScriptV1TokenType.BOOLEAN_TYPE, "boolean"),
                ExpectedToken(PrintScriptV1TokenType.ASSIGN, "="),
                ExpectedToken(PrintScriptV1TokenType.TRUE, "true"),
                ExpectedToken(PrintScriptV1TokenType.SEMICOLON, ";"),
                ExpectedToken(PrintScriptV1TokenType.IF, "if"),
                ExpectedToken(PrintScriptV1TokenType.LEFT_PAREN, "("),
                ExpectedToken(PrintScriptV1TokenType.IDENTIFIER, "active"),
                ExpectedToken(PrintScriptV1TokenType.RIGHT_PAREN, ")"),
                ExpectedToken(PrintScriptV1TokenType.LEFT_BRACE, "{"),
                ExpectedToken(PrintScriptV1TokenType.LET, "let"),
                ExpectedToken(PrintScriptV1TokenType.IDENTIFIER, "name"),
                ExpectedToken(PrintScriptV1TokenType.COLON, ":"),
                ExpectedToken(PrintScriptV1TokenType.STRING_TYPE, "string"),
                ExpectedToken(PrintScriptV1TokenType.ASSIGN, "="),
                ExpectedToken(PrintScriptV1TokenType.READ_INPUT, "readInput"),
                ExpectedToken(PrintScriptV1TokenType.LEFT_PAREN, "("),
                ExpectedToken(PrintScriptV1TokenType.STRING_LITERAL, "\"Name:\""),
                ExpectedToken(PrintScriptV1TokenType.RIGHT_PAREN, ")"),
                ExpectedToken(PrintScriptV1TokenType.SEMICOLON, ";"),
                ExpectedToken(PrintScriptV1TokenType.LET, "let"),
                ExpectedToken(PrintScriptV1TokenType.IDENTIFIER, "club"),
                ExpectedToken(PrintScriptV1TokenType.COLON, ":"),
                ExpectedToken(PrintScriptV1TokenType.STRING_TYPE, "string"),
                ExpectedToken(PrintScriptV1TokenType.ASSIGN, "="),
                ExpectedToken(PrintScriptV1TokenType.READ_ENV, "readEnv"),
                ExpectedToken(PrintScriptV1TokenType.LEFT_PAREN, "("),
                ExpectedToken(PrintScriptV1TokenType.STRING_LITERAL, "\"BEST_FOOTBALL_CLUB\""),
                ExpectedToken(PrintScriptV1TokenType.RIGHT_PAREN, ")"),
                ExpectedToken(PrintScriptV1TokenType.SEMICOLON, ";"),
                ExpectedToken(PrintScriptV1TokenType.RIGHT_BRACE, "}"),
                ExpectedToken(PrintScriptV1TokenType.ELSE, "else"),
                ExpectedToken(PrintScriptV1TokenType.LEFT_BRACE, "{"),
                ExpectedToken(PrintScriptV1TokenType.PRINTLN, "println"),
                ExpectedToken(PrintScriptV1TokenType.LEFT_PAREN, "("),
                ExpectedToken(PrintScriptV1TokenType.FALSE, "false"),
                ExpectedToken(PrintScriptV1TokenType.RIGHT_PAREN, ")"),
                ExpectedToken(PrintScriptV1TokenType.SEMICOLON, ";"),
                ExpectedToken(PrintScriptV1TokenType.RIGHT_BRACE, "}"),
                ExpectedToken(PrintScriptV1TokenType.EOF, ""),
            ),
        )
    }

    @Test
    fun `recognizes V1_1 tokens across source chunks`() {
        lexer.tokenize(
            sourceReaderForChunks(
                "co",
                "nst flag: boole",
                "an = tr",
                "ue; {",
                "}",
            ),
        ).assertProducesTokenSequence(
            listOf(
                ExpectedToken(PrintScriptV1TokenType.CONST, "const"),
                ExpectedToken(PrintScriptV1TokenType.IDENTIFIER, "flag"),
                ExpectedToken(PrintScriptV1TokenType.COLON, ":"),
                ExpectedToken(PrintScriptV1TokenType.BOOLEAN_TYPE, "boolean"),
                ExpectedToken(PrintScriptV1TokenType.ASSIGN, "="),
                ExpectedToken(PrintScriptV1TokenType.TRUE, "true"),
                ExpectedToken(PrintScriptV1TokenType.SEMICOLON, ";"),
                ExpectedToken(PrintScriptV1TokenType.LEFT_BRACE, "{"),
                ExpectedToken(PrintScriptV1TokenType.RIGHT_BRACE, "}"),
                ExpectedToken(PrintScriptV1TokenType.EOF, ""),
            ),
        )
    }
}
