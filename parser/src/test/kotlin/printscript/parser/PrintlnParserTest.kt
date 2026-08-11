package printscript.parser

import printscript.model.ast.expression.BinaryExpression
import printscript.model.ast.expression.BinaryOperator
import printscript.model.ast.expression.IdentifierExpression
import printscript.model.ast.expression.NumberLiteralExpression
import printscript.model.ast.expression.StringLiteralExpression
import printscript.model.ast.statement.PrintlnStatement
import printscript.statement.StatementReadResult
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrintlnParserTest {

    @Test
    fun `println of a literal`() {
        val statement = statementOf(
            tokens { println(); open(); number("168"); close(); semicolon(); eof() },
        ) as PrintlnStatement
        assertEquals(BigDecimal("168"), (statement.argument as NumberLiteralExpression).value)
    }

    @Test
    fun `println of a concatenation`() {
        val statement = statementOf(
            tokens { println(); open(); string("\"Result: \""); plus(); id("c"); close(); semicolon(); eof() },
        ) as PrintlnStatement
        val concat = statement.argument as BinaryExpression
        assertEquals(BinaryOperator.ADD, concat.operator)
        assertTrue(concat.left is StringLiteralExpression)
        assertTrue(concat.right is IdentifierExpression)
    }

    @Test
    fun `missing open paren fails`() {
        val result = parseFirst(
            tokens { println(); number("1"); close(); semicolon(); eof() },
        )
        assertTrue(result is StatementReadResult.Failure)
    }

    @Test
    fun `missing close paren fails`() {
        val result = parseFirst(
            tokens { println(); open(); number("1"); semicolon(); eof() },
        )
        assertTrue(result is StatementReadResult.Failure)
    }

    @Test
    fun `missing semicolon fails`() {
        val result = parseFirst(
            tokens { println(); open(); number("1"); close(); eof() },
        )
        assertTrue(result is StatementReadResult.Failure)
    }
}