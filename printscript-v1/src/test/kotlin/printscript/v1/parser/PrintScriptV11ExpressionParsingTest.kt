package printscript.v1.parser

import printscript.ast.expression.BinaryExpression
import printscript.ast.expression.BooleanLiteralExpression
import printscript.ast.expression.ReadEnvironmentExpression
import printscript.ast.expression.ReadInputExpression
import printscript.ast.expression.StringLiteralExpression
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class PrintScriptV11ExpressionParsingTest {

    @Test
    fun `parses boolean literals`() {
        val trueExpression = assertIs<BooleanLiteralExpression>(
            expressionOfV11 { trueLiteral() },
        )
        val falseExpression = assertIs<BooleanLiteralExpression>(
            expressionOfV11 { falseLiteral() },
        )

        assertTrue(trueExpression.value)
        assertFalse(falseExpression.value)
    }

    @Test
    fun `parses read input with its prompt`() {
        val expression = assertIs<ReadInputExpression>(
            expressionOfV11 {
                readInput()
                open()
                string("\"Name:\"")
                close()
            },
        )
        val prompt = assertIs<StringLiteralExpression>(expression.prompt)

        assertEquals(
            expected = "Name:",
            actual = prompt.value,
        )
    }

    @Test
    fun `parses read environment with its variable name`() {
        val expression = assertIs<ReadEnvironmentExpression>(
            expressionOfV11 {
                readEnvironment()
                open()
                string("\"BEST_FOOTBALL_CLUB\"")
                close()
            },
        )
        val variableName = assertIs<StringLiteralExpression>(expression.variableName)

        assertEquals(
            expected = "BEST_FOOTBALL_CLUB",
            actual = variableName.value,
        )
    }

    @Test
    fun `read input preserves a composed prompt for later validation`() {
        val expression = assertIs<ReadInputExpression>(
            expressionOfV11 {
                readInput()
                open()
                string("\"Enter \"")
                plus()
                string("\"something\"")
                close()
            },
        )

        assertIs<BinaryExpression>(expression.prompt)
    }
}
