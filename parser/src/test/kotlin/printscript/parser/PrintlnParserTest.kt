package printscript.parser

import printscript.ast.statement.PrintlnStatement
import printscript.ast.expression.BinaryExpression
import printscript.ast.expression.BinaryOperator
import printscript.ast.expression.StringLiteralExpression
import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan
import printscript.token.TokenReadResult
import printscript.token.TokenType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class PrintlnParserTest {

    @Test
    fun `parses println with a literal argument`() {
        val statement = statementOf<PrintlnStatement>(
            tokens {
                println()
                open()
                string("\"hola\"")
                close()
                semicolon()
                eof()
            },
        )

        val argument = assertIs<StringLiteralExpression>(statement.argument)

        assertEquals(
            expected = "hola",
            actual = argument.value,
        )
    }

    @Test
    fun `parses println with a composed argument`() {
        val statement = statementOf<PrintlnStatement>(
            tokens {
                println()
                open()
                id("name")
                plus()
                string("\" Doe\"")
                close()
                semicolon()
                eof()
            },
        )

        val argument = assertIs<BinaryExpression>(statement.argument)

        assertEquals(
            expected = BinaryOperator.ADD,
            actual = argument.operator,
        )
    }

    @Test
    fun `statement span covers from the keyword to the semicolon`() {
        // "println ( a ) ;"
        //  ^              ^
        //  col 1          col 16
        val statement = statementOf<PrintlnStatement>(
            tokens {
                println()
                open()
                id("a")
                close()
                semicolon()
                eof()
            },
        )

        val expectedSpan = SourceSpan(
            start = SourcePosition(line = 1, column = 1, offset = 0),
            end = SourcePosition(line = 1, column = 16, offset = 15),
        )

        assertEquals(
            expected = expectedSpan,
            actual = statement.span,
        )
    }

    @Test
    fun `rejects malformed println calls`() {
        val malformedCalls = listOf(
            MalformedPrintln(
                description = "sin paréntesis de apertura",
                tokens = tokens {
                    println()
                    id("a")
                    close()
                    semicolon()
                    eof()
                },
                expectedTokenTypes = setOf(TokenType.LEFT_PAREN),
                actualTokenType = TokenType.IDENTIFIER,
            ),
            MalformedPrintln(
                description = "sin paréntesis de cierre",
                tokens = tokens {
                    println()
                    open()
                    id("a")
                    semicolon()
                    eof()
                },
                expectedTokenTypes = setOf(TokenType.RIGHT_PAREN),
                actualTokenType = TokenType.SEMICOLON,
            ),
            MalformedPrintln(
                description = "sin punto y coma",
                tokens = tokens {
                    println()
                    open()
                    id("a")
                    close()
                    eof()
                },
                expectedTokenTypes = setOf(TokenType.SEMICOLON),
                actualTokenType = TokenType.EOF,
            ),
        )

        for (malformedCall in malformedCalls) {
            assertRejects(malformedCall)
        }
    }

    private fun assertRejects(
        malformedPrintln: MalformedPrintln,
    ) {
        parseFirst(malformedPrintln.tokens).assertUnexpectedToken(
            expectedTokenTypes = malformedPrintln.expectedTokenTypes,
            actualTokenType = malformedPrintln.actualTokenType,
        )
    }

    private data class MalformedPrintln(
        val description: String,
        val tokens: List<TokenReadResult>,
        val expectedTokenTypes: Set<TokenType>,
        val actualTokenType: TokenType,
    )
}