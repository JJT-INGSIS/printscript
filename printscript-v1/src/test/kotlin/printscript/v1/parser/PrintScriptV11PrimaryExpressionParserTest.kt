package printscript.v1.parser

import printscript.statement.ParseError
import printscript.v1.token.PrintScriptV1TokenType
import kotlin.test.Test

class PrintScriptV11PrimaryExpressionParserTest {

    @Test
    fun `reports both V1 and V1_1 alternatives when a primary expression is missing`() {
        parseFirstV11(
            tokens {
                id("target")
                assign()
                semicolon()
                eof()
            },
        ).assertUnexpectedToken(
            expectedTokenTypes = setOf(
                PrintScriptV1TokenType.NUMBER_LITERAL,
                PrintScriptV1TokenType.STRING_LITERAL,
                PrintScriptV1TokenType.IDENTIFIER,
                PrintScriptV1TokenType.LEFT_PAREN,
                PrintScriptV1TokenType.TRUE,
                PrintScriptV1TokenType.FALSE,
                PrintScriptV1TokenType.READ_INPUT,
                PrintScriptV1TokenType.READ_ENV,
            ),
            actualTokenType = PrintScriptV1TokenType.SEMICOLON,
        )
    }

    @Test
    fun `still reports an invalid literal instead of widening its alternatives`() {
        parseFirstV11(
            tokens {
                id("target")
                assign()
                number("1..2")
                semicolon()
                eof()
            },
        ).assertParseError<ParseError.InvalidLiteral>()
    }
}
