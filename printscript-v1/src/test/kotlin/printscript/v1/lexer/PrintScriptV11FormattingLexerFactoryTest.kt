package printscript.v1.lexer

import printscript.v1.token.PrintScriptV1TokenType
import kotlin.test.Test

class PrintScriptV11FormattingLexerFactoryTest {

    @Test
    fun `preserves whitespace while recognizing V1_1 tokens`() {
        PrintScriptV11FormattingLexerFactory.create()
            .tokenize(sourceReaderFor("if (active) {\n  println(false);\n}"))
            .assertProducesTokenSequence(
                listOf(
                    ExpectedToken(PrintScriptV1TokenType.IF, "if"),
                    ExpectedToken(PrintScriptV1FormattingTokenType.WHITESPACE, " "),
                    ExpectedToken(PrintScriptV1TokenType.LEFT_PAREN, "("),
                    ExpectedToken(PrintScriptV1TokenType.IDENTIFIER, "active"),
                    ExpectedToken(PrintScriptV1TokenType.RIGHT_PAREN, ")"),
                    ExpectedToken(PrintScriptV1FormattingTokenType.WHITESPACE, " "),
                    ExpectedToken(PrintScriptV1TokenType.LEFT_BRACE, "{"),
                    ExpectedToken(PrintScriptV1FormattingTokenType.WHITESPACE, "\n  "),
                    ExpectedToken(PrintScriptV1TokenType.PRINTLN, "println"),
                    ExpectedToken(PrintScriptV1TokenType.LEFT_PAREN, "("),
                    ExpectedToken(PrintScriptV1TokenType.FALSE, "false"),
                    ExpectedToken(PrintScriptV1TokenType.RIGHT_PAREN, ")"),
                    ExpectedToken(PrintScriptV1TokenType.SEMICOLON, ";"),
                    ExpectedToken(PrintScriptV1FormattingTokenType.WHITESPACE, "\n"),
                    ExpectedToken(PrintScriptV1TokenType.RIGHT_BRACE, "}"),
                    ExpectedToken(PrintScriptV1TokenType.EOF, ""),
                ),
            )
    }
}
