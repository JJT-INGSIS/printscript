package printscript.parser

import printscript.model.ast.expression.NumberLiteralExpression
import printscript.model.ast.statement.AssignmentStatement
import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan
import printscript.token.TokenReadResult
import printscript.token.TokenType
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
        // "a = 5 ;"
        //  ^      ^
        //  col 1  col 8
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
                expectedTokenTypes = setOf(TokenType.ASSIGN),
                actualTokenType = TokenType.PLUS,
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
                    TokenType.NUMBER_LITERAL,
                    TokenType.STRING_LITERAL,
                    TokenType.IDENTIFIER,
                    TokenType.LEFT_PAREN,
                ),
                actualTokenType = TokenType.SEMICOLON,
            ),
            MalformedAssignment(
                description = "sin punto y coma",
                tokens = tokens {
                    id("a")
                    assign()
                    number("5")
                    eof()
                },
                expectedTokenTypes = setOf(TokenType.SEMICOLON),
                actualTokenType = TokenType.EOF,
            ),
        )

        for (malformedAssignment in malformedAssignments) {
            assertRejects(malformedAssignment)
        }
    }

    private fun assertRejects(
        malformedAssignment: MalformedAssignment,
    ) {
        parseFirst(malformedAssignment.tokens).assertUnexpectedToken(
            expectedTokenTypes = malformedAssignment.expectedTokenTypes,
            actualTokenType = malformedAssignment.actualTokenType,
        )
    }

    private data class MalformedAssignment(
        val description: String,
        val tokens: List<TokenReadResult>,
        val expectedTokenTypes: Set<TokenType>,
        val actualTokenType: TokenType,
    )
}