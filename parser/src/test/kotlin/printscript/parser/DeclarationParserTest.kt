package printscript.parser

import printscript.model.ast.DeclaredType
import printscript.model.ast.expression.NumberLiteralExpression
import printscript.model.ast.statement.VariableDeclarationStatement
import printscript.statement.StatementReadResult
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DeclarationParserTest {

    @Test
    fun `let with number type and initializer`() {
        // given / when
        val statement = statementOf(
            tokens { let(); id("a"); colon(); numberType(); assign(); number("5"); semicolon(); eof() },
        ) as VariableDeclarationStatement
        // then
        assertEquals("a", statement.identifier.value)
        assertEquals(DeclaredType.NUMBER, statement.declaredType)
        assertEquals(BigDecimal("5"), (statement.initializer as NumberLiteralExpression).value)
    }

    @Test
    fun `let with string type`() {
        val statement = statementOf(
            tokens { let(); id("name"); colon(); stringType(); semicolon(); eof() },
        ) as VariableDeclarationStatement
        assertEquals(DeclaredType.STRING, statement.declaredType)
    }

    @Test
    fun `declaration without initializer is null`() {
        val statement = statementOf(
            tokens { let(); id("a"); colon(); numberType(); semicolon(); eof() },
        ) as VariableDeclarationStatement
        assertNull(statement.initializer)
    }

    @Test
    fun `invalid type fails`() {
        val result = parseFirst(
            tokens { let(); id("a"); colon(); id("notAType"); semicolon(); eof() },
        )
        assertTrue(result is StatementReadResult.Failure)
    }

    @Test
    fun `missing colon fails`() {
        val result = parseFirst(
            tokens { let(); id("a"); numberType(); semicolon(); eof() },
        )
        assertTrue(result is StatementReadResult.Failure)
    }

    @Test
    fun `missing semicolon fails`() {
        val result = parseFirst(
            tokens { let(); id("a"); colon(); numberType(); eof() },
        )
        assertTrue(result is StatementReadResult.Failure)
    }

    @Test
    fun `missing identifier fails`() {
        val result = parseFirst(
            tokens { let(); colon(); numberType(); semicolon(); eof() },
        )
        assertTrue(result is StatementReadResult.Failure)
    }
}