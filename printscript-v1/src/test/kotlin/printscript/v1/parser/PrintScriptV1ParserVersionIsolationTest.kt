package printscript.v1.parser

import printscript.v1.token.PrintScriptV1TokenType
import kotlin.test.Test

class PrintScriptV1ParserVersionIsolationTest {

    @Test
    fun `V1 rejects boolean declarations`() {
        parseFirst(
            tokens {
                let()
                id("active")
                colon()
                booleanType()
                assign()
                trueLiteral()
                semicolon()
                eof()
            },
        ).assertUnexpectedToken(
            expectedTokenTypes = setOf(
                PrintScriptV1TokenType.NUMBER_TYPE,
                PrintScriptV1TokenType.STRING_TYPE,
            ),
            actualTokenType = PrintScriptV1TokenType.BOOLEAN_TYPE,
        )
    }

    @Test
    fun `V1 rejects constant declarations`() {
        parseFirst(
            tokens {
                constant()
                id("name")
                colon()
                stringType()
                assign()
                string("\"PrintScript\"")
                semicolon()
                eof()
            },
        ).assertUnexpectedToken(
            expectedTokenTypes = setOf(
                PrintScriptV1TokenType.LET,
                PrintScriptV1TokenType.PRINTLN,
                PrintScriptV1TokenType.IDENTIFIER,
            ),
            actualTokenType = PrintScriptV1TokenType.CONST,
        )
    }

    @Test
    fun `V1 rejects if statements`() {
        parseFirst(
            tokens {
                ifKeyword()
                open()
                id("active")
                close()
                leftBrace()
                rightBrace()
                eof()
            },
        ).assertUnexpectedToken(
            expectedTokenTypes = setOf(
                PrintScriptV1TokenType.LET,
                PrintScriptV1TokenType.PRINTLN,
                PrintScriptV1TokenType.IDENTIFIER,
            ),
            actualTokenType = PrintScriptV1TokenType.IF,
        )
    }

    @Test
    fun `V1 rejects boolean expressions`() {
        parseFirst(
            tokens {
                id("active")
                assign()
                trueLiteral()
                semicolon()
                eof()
            },
        ).assertUnexpectedToken(
            expectedTokenTypes = setOf(
                PrintScriptV1TokenType.STRING_LITERAL,
                PrintScriptV1TokenType.NUMBER_LITERAL,
                PrintScriptV1TokenType.IDENTIFIER,
                PrintScriptV1TokenType.LEFT_PAREN,
            ),
            actualTokenType = PrintScriptV1TokenType.TRUE,
        )
    }
}
