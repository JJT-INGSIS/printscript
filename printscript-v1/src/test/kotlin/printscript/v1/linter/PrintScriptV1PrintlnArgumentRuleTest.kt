package printscript.v1.linter

import printscript.ast.DeclaredType
import printscript.ast.expression.Expression
import printscript.linter.Diagnostic
import printscript.linter.Linter
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class PrintScriptV1PrintlnArgumentRuleTest {

    private val linter: Linter = linterWith(
        printlnArgumentRule(),
    )

    private fun diagnosticsForArgument(argument: Expression): List<Diagnostic> {
        return diagnosticsOf(linter, printOf(argument))
    }

    @Test
    fun `accepts a variable as argument`() {
        val argument = variable("total")

        val diagnostics = diagnosticsForArgument(argument)

        diagnostics.assertNoDiagnostics()
    }

    @Test
    fun `accepts a number literal as argument`() {
        val argument = number("42")

        val diagnostics = diagnosticsForArgument(argument)

        diagnostics.assertNoDiagnostics()
    }

    @Test
    fun `accepts a string literal as argument`() {
        val argument = text("hello")

        val diagnostics = diagnosticsForArgument(argument)

        diagnostics.assertNoDiagnostics()
    }

    @Test
    fun `reports a binary expression as argument`() {
        val argument = sum(number("2"), number("3"))

        val diagnostics = diagnosticsForArgument(argument)

        val violation = assertIs<PrintScriptV1Diagnostic.UnsupportedPrintlnArgument>(
            diagnostics.single(),
        )

        assertEquals(
            expected = argument,
            actual = violation.argument,
        )
    }

    @Test
    fun `reports a unary expression as argument`() {
        val argument = negated(number("2"))

        val diagnostics = diagnosticsForArgument(argument)

        assertIs<PrintScriptV1Diagnostic.UnsupportedPrintlnArgument>(
            diagnostics.single(),
        )
    }

    @Test
    fun `reports a grouped expression as argument`() {
        val argument = grouped(variable("total"))

        val diagnostics = diagnosticsForArgument(argument)

        assertIs<PrintScriptV1Diagnostic.UnsupportedPrintlnArgument>(
            diagnostics.single(),
        )
    }

    @Test
    fun `accepts a composed argument when the configuration allows it`() {
        val permissiveLinter = linterWith(
            PrintScriptV1RuleConfiguration.PrintlnArgument(
                acceptanceByKind = mapOf(
                    PrintScriptV1ExpressionKind.LITERAL to PrintScriptV1ArgumentAcceptance.ACCEPTED,
                    PrintScriptV1ExpressionKind.VARIABLE to PrintScriptV1ArgumentAcceptance.ACCEPTED,
                    PrintScriptV1ExpressionKind.COMPOSED to PrintScriptV1ArgumentAcceptance.ACCEPTED,
                ),
            ),
        )

        val argument = sum(number("2"), number("3"))

        val diagnostics = diagnosticsOf(permissiveLinter, printOf(argument))

        diagnostics.assertNoDiagnostics()
    }

    @Test
    fun `rejects a literal argument when the configuration forbids it`() {
        val strictLinter = linterWith(
            PrintScriptV1RuleConfiguration.PrintlnArgument(
                acceptanceByKind = mapOf(
                    PrintScriptV1ExpressionKind.LITERAL to PrintScriptV1ArgumentAcceptance.REJECTED,
                    PrintScriptV1ExpressionKind.VARIABLE to PrintScriptV1ArgumentAcceptance.ACCEPTED,
                    PrintScriptV1ExpressionKind.COMPOSED to PrintScriptV1ArgumentAcceptance.REJECTED,
                ),
            ),
        )

        val diagnostics = diagnosticsOf(strictLinter, printOf(number("42")))

        assertIs<PrintScriptV1Diagnostic.UnsupportedPrintlnArgument>(
            diagnostics.single(),
        )
    }

    @Test
    fun `fails to build when the configuration leaves a kind uncovered`() {
        val incompleteConfiguration = PrintScriptV1RuleConfiguration.PrintlnArgument(
            acceptanceByKind = mapOf(
                PrintScriptV1ExpressionKind.LITERAL to PrintScriptV1ArgumentAcceptance.ACCEPTED,
            ),
        )

        val failure = assertFailsWith<IllegalArgumentException> {
            linterWith(incompleteConfiguration)
        }

        assertContains(
            charSequence = failure.message.orEmpty(),
            other = PrintScriptV1ExpressionKind.COMPOSED.name,
        )
    }

    @Test
    fun `ignores statements that are not a println`() {
        val expression = sum(number("2"), number("3"))

        val diagnostics = diagnosticsOf(
            linter,
            declare("total", DeclaredType.NUMBER, expression),
            assign("total", expression),
        )

        diagnostics.assertNoDiagnostics()
    }
}
