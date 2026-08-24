package printscript.linter

import printscript.ast.DeclaredType
import printscript.ast.expression.Expression
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class PrintlnArgumentRuleTest {

    private val linter: Linter = linterWith(
        RuleConfiguration.PrintlnArgument,
    )

    private fun diagnosticsForArgument(
        argument: Expression,
    ): List<Diagnostic> {
        return diagnosticsOf(linter, printOf(argument))
    }

    @Test
    fun `accepts a variable as argument`() {
        // given
        val argument = variable("total")

        // when
        val diagnostics = diagnosticsForArgument(argument)

        // then
        diagnostics.assertNoDiagnostics()
    }

    @Test
    fun `accepts a number literal as argument`() {
        // given
        val argument = number("42")

        // when
        val diagnostics = diagnosticsForArgument(argument)

        // then
        diagnostics.assertNoDiagnostics()
    }

    @Test
    fun `accepts a string literal as argument`() {
        // given
        val argument = text("hello")

        // when
        val diagnostics = diagnosticsForArgument(argument)

        // then
        diagnostics.assertNoDiagnostics()
    }

    @Test
    fun `reports a binary expression as argument`() {
        // given
        val argument = sum(number("2"), number("3"))

        // when
        val diagnostics = diagnosticsForArgument(argument)

        // then
        val violation = assertIs<Diagnostic.UnsupportedPrintlnArgument>(
            diagnostics.single(),
        )

        assertEquals(
            expected = argument,
            actual = violation.argument,
        )
    }

    @Test
    fun `reports a unary expression as argument`() {
        // given
        val argument = negated(number("2"))

        // when
        val diagnostics = diagnosticsForArgument(argument)

        // then
        assertIs<Diagnostic.UnsupportedPrintlnArgument>(
            diagnostics.single(),
        )
    }

    @Test
    fun `reports a grouped expression as argument`() {
        // given
        val argument = grouped(variable("total"))

        // when
        val diagnostics = diagnosticsForArgument(argument)

        // then
        assertIs<Diagnostic.UnsupportedPrintlnArgument>(
            diagnostics.single(),
        )
    }

    @Test
    fun `ignores statements that are not a println`() {
        // given
        val expression = sum(number("2"), number("3"))

        // when
        val diagnostics = diagnosticsOf(
            linter,
            declare("total", DeclaredType.NUMBER, expression),
            assign("total", expression),
        )

        // then
        diagnostics.assertNoDiagnostics()
    }
}
