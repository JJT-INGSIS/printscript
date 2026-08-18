package printscript.parser

import printscript.ast.DeclaredType
import printscript.ast.expression.NumberLiteralExpression
import printscript.ast.statement.VariableDeclarationStatement
import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan
import printscript.token.TokenReadResult
import printscript.token.TokenType
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class DeclarationParserTest {

    @Test
    fun `parses declaration with number type and initializer`() {
        val statement = statementOf<VariableDeclarationStatement>(
            tokens {
                let()
                id("a")
                colon()
                numberType()
                assign()
                number("5")
                semicolon()
                eof()
            },
        )

        assertEquals(
            expected = "a",
            actual = statement.identifier.value,
        )

        assertEquals(
            expected = DeclaredType.NUMBER,
            actual = statement.declaredType,
        )

        val initializer = assertIs<NumberLiteralExpression>(statement.initializer)

        assertEquals(
            expected = BigDecimal("5"),
            actual = initializer.value,
        )
    }

    @Test
    fun `parses declaration with string type`() {
        val statement = statementOf<VariableDeclarationStatement>(
            tokens {
                let()
                id("name")
                colon()
                stringType()
                semicolon()
                eof()
            },
        )

        assertEquals(
            expected = DeclaredType.STRING,
            actual = statement.declaredType,
        )
    }

    @Test
    fun `declaration without initializer has no expression`() {
        val statement = statementOf<VariableDeclarationStatement>(
            tokens {
                let()
                id("a")
                colon()
                numberType()
                semicolon()
                eof()
            },
        )

        assertNull(statement.initializer)
    }

    @Test
    fun `statement span covers from the keyword to the semicolon`() {
        // "let a : number = 5 ;"
        //  ^                   ^
        //  columna 1           columna 21
        val statement = statementOf<VariableDeclarationStatement>(
            tokens {
                let()
                id("a")
                colon()
                numberType()
                assign()
                number("5")
                semicolon()
                eof()
            },
        )

        val expectedSpan = SourceSpan(
            start = SourcePosition(line = 1, column = 1, offset = 0),
            end = SourcePosition(line = 1, column = 21, offset = 20),
        )

        assertEquals(
            expected = expectedSpan,
            actual = statement.span,
        )
    }

    @Test
    fun `rejects malformed declarations`() {
        val malformedDeclarations = listOf(
            MalformedDeclaration(
                description = "sin identificador",
                tokens = tokens {
                    let()
                    colon()
                    numberType()
                    semicolon()
                    eof()
                },
                expectedTokenTypes = setOf(TokenType.IDENTIFIER),
                actualTokenType = TokenType.COLON,
            ),
            MalformedDeclaration(
                description = "sin dos puntos",
                tokens = tokens {
                    let()
                    id("a")
                    numberType()
                    semicolon()
                    eof()
                },
                expectedTokenTypes = setOf(TokenType.COLON),
                actualTokenType = TokenType.NUMBER_TYPE,
            ),
            MalformedDeclaration(
                description = "con un tipo inexistente",
                tokens = tokens {
                    let()
                    id("a")
                    colon()
                    id("notAType")
                    semicolon()
                    eof()
                },
                expectedTokenTypes = setOf(
                    TokenType.NUMBER_TYPE,
                    TokenType.STRING_TYPE,
                ),
                actualTokenType = TokenType.IDENTIFIER,
            ),
            MalformedDeclaration(
                description = "sin punto y coma",
                tokens = tokens {
                    let()
                    id("a")
                    colon()
                    numberType()
                    eof()
                },
                expectedTokenTypes = setOf(TokenType.SEMICOLON),
                actualTokenType = TokenType.EOF,
            ),
        )

        for (malformedDeclaration in malformedDeclarations) {
            assertRejects(malformedDeclaration)
        }
    }

    private fun assertRejects(
        malformedDeclaration: MalformedDeclaration,
    ) {
        parseFirst(malformedDeclaration.tokens).assertUnexpectedToken(
            expectedTokenTypes = malformedDeclaration.expectedTokenTypes,
            actualTokenType = malformedDeclaration.actualTokenType,
        )
    }

    private data class MalformedDeclaration(
        val description: String,
        val tokens: List<TokenReadResult>,
        val expectedTokenTypes: Set<TokenType>,
        val actualTokenType: TokenType,
    )
}