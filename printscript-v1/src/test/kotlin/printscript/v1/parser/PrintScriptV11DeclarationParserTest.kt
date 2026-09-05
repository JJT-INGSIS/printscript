package printscript.v1.parser

import printscript.ast.DeclarationKind
import printscript.ast.DeclaredType
import printscript.ast.expression.BooleanLiteralExpression
import printscript.ast.statement.VariableDeclarationStatement
import printscript.v1.token.PrintScriptV1TokenType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class PrintScriptV11DeclarationParserTest {

    @Test
    fun `parses a boolean variable declaration`() {
        val statement = statementOfV11<VariableDeclarationStatement>(
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
        )

        assertEquals(DeclarationKind.VARIABLE, statement.declarationKind)
        assertEquals(DeclaredType.BOOLEAN, statement.declaredType)
        assertTrue(assertIs<BooleanLiteralExpression>(statement.initializer).value)
    }

    @Test
    fun `parses a constant declaration`() {
        val statement = statementOfV11<VariableDeclarationStatement>(
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
        )

        assertEquals(DeclarationKind.CONSTANT, statement.declarationKind)
        assertEquals(DeclaredType.STRING, statement.declaredType)
    }

    @Test
    fun `constant declarations require an initializer`() {
        parseFirstV11(
            tokens {
                constant()
                id("value")
                colon()
                numberType()
                semicolon()
                eof()
            },
        ).assertUnexpectedToken(
            expectedTokenTypes = setOf(PrintScriptV1TokenType.ASSIGN),
            actualTokenType = PrintScriptV1TokenType.SEMICOLON,
        )
    }
}
