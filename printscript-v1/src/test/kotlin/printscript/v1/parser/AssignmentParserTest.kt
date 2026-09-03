package printscript.v1.parser

import printscript.ast.expression.NumberLiteralExpression
import printscript.ast.statement.AssignmentStatement
import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan
import printscript.token.TokenType
import printscript.v1.token.PrintScriptV1TokenType
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class AssignmentParserTest {

    @Test
    fun `parses assignment with literal value`() {
        val statement = statementOf<AssignmentStatement>(
            tokens {
                id("a")
                assign()
                number("5")
                semicolon()
                eof()
            },
        )

        assertEquals(
            expected = "a",
            actual = statement.target.value,
        )

        val value = assertIs<NumberLiteralExpression>(statement.expression)

        assertEquals(
            expected = BigDecimal("5"),
            actual = value.value,
        )
    }

    @Test
    fun `statement span covers from the target to the semicolon`() {
        val statement = statementOf<AssignmentStatement>(
            tokens {
                id("a")
                assign()
                number("5")
                semicolon()
                eof()
            },
        )

        val expectedSpan = SourceSpan(
            start = SourcePosition(line = 1, column = 1, offset = 0),
            end = SourcePosition(line = 1, column = 8, offset = 7),
        )

        assertEquals(
            expected = expectedSpan,
            actual = statement.span,
        )
    }

    @Test
    fun `rejects malformed assignments`() {
        val malformedAssignments = listOf(
            MalformedAssignment(
                description = "sin operador de asignación",
                tokens = tokens {
                    id("a")
                    plus()
                    number("5")
                    semicolon()
                    eof()
                },
                expectedTokenTypes = setOf(PrintScriptV1TokenType.ASSIGN),
                actualTokenType = PrintScriptV1TokenType.PLUS,
            ),
            MalformedAssignment(
                description = "sin expresión",
                tokens = tokens {
                    id("a")
                    assign()
                    semicolon()
                    eof()
                },
                expectedTokenTypes = setOf(
                    PrintScriptV1TokenType.NUMBER_LITERAL,
                    PrintScriptV1TokenType.STRING_LITERAL,
                    PrintScriptV1TokenType.IDENTIFIER,
                    PrintScriptV1TokenType.LEFT_PAREN,
                ),
                actualTokenType = PrintScriptV1TokenType.SEMICOLON,
            ),
            MalformedAssignment(
                description = "sin punto y coma",
                tokens = tokens {
                    id("a")
                    assign()
                    number("5")
                    eof()
                },
                expectedTokenTypes = setOf(PrintScriptV1TokenType.SEMICOLON),
                actualTokenType = PrintScriptV1TokenType.EOF,
            ),
        )

        for (malformedAssignment in malformedAssignments) {
            assertRejects(malformedAssignment)
        }
    }

    private fun assertRejects(malformedAssignment: MalformedAssignment) {
        parseFirst(malformedAssignment.tokens).assertUnexpectedToken(
            expectedTokenTypes = malformedAssignment.expectedTokenTypes,
            actualTokenType = malformedAssignment.actualTokenType,
            message = malformedAssignment.description,
        )
    }

    private data class MalformedAssignment(
        val description: String,
        val tokens: List<TokenReadFixture>,
        val expectedTokenTypes: Set<TokenType>,
        val actualTokenType: TokenType,
    )
}
