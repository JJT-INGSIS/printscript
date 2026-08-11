package printscript.parser

import printscript.model.ast.expression.BinaryExpression
import printscript.model.ast.expression.BinaryOperator
import printscript.model.ast.expression.IdentifierExpression
import printscript.model.ast.statement.AssignmentStatement
import printscript.statement.StatementReadResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AssignmentParserTest {

    @Test
    fun `assign an identifier`() {
        val statement = statementOf(
            tokens { id("x"); assign(); id("y"); semicolon(); eof() },
        ) as AssignmentStatement
        assertEquals("x", statement.target.value)
        assertEquals("y", (statement.expression as IdentifierExpression).identifier.value)
    }

    @Test
    fun `assign an expression`() {
        val statement = statementOf(
            tokens { id("a"); assign(); id("a"); slash(); id("b"); semicolon(); eof() },
        ) as AssignmentStatement
        assertEquals(BinaryOperator.DIVIDE, (statement.expression as BinaryExpression).operator)
    }

    @Test
    fun `missing equals fails`() {
        val result = parseFirst(
            tokens { id("x"); number("5"); semicolon(); eof() },
        )
        assertTrue(result is StatementReadResult.Failure)
    }

    @Test
    fun `missing expression fails`() {
        val result = parseFirst(
            tokens { id("x"); assign(); semicolon(); eof() },
        )
        assertTrue(result is StatementReadResult.Failure)
    }

    @Test
    fun `missing semicolon fails`() {
        val result = parseFirst(
            tokens { id("x"); assign(); number("5"); eof() },
        )
        assertTrue(result is StatementReadResult.Failure)
    }
}