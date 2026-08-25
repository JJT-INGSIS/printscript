package printscript.linter

import printscript.ast.DeclaredType
import printscript.ast.expression.Expression
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class PrintlnArgumentRuleTest {

    private val linter: Linter = linterWith(
        printlnArgumentRule(),
    )

    private fun diagnosticsForArgument(argument: Expression): List<Diagnostic> {
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

    /**
     * La política es dato: invertirla no toca la regla.
     */
    @Test
    fun `accepts a composed argument when the configuration allows it`() {
        // given
        val permissiveLinter = linterWith(
            RuleConfiguration.PrintlnArgument(
                acceptanceByKind = mapOf(
                    ExpressionKind.LITERAL to ArgumentAcceptance.ACCEPTED,
                    ExpressionKind.VARIABLE to ArgumentAcceptance.ACCEPTED,
                    ExpressionKind.COMPOSED to ArgumentAcceptance.ACCEPTED,
                ),
            ),
        )

        val argument = sum(number("2"), number("3"))

        // when
        val diagnostics = diagnosticsOf(permissiveLinter, printOf(argument))

        // then
        diagnostics.assertNoDiagnostics()
    }

    @Test
    fun `rejects a literal argument when the configuration forbids it`() {
        // given
        val strictLinter = linterWith(
            RuleConfiguration.PrintlnArgument(
                acceptanceByKind = mapOf(
                    ExpressionKind.LITERAL to ArgumentAcceptance.REJECTED,
                    ExpressionKind.VARIABLE to ArgumentAcceptance.ACCEPTED,
                    ExpressionKind.COMPOSED to ArgumentAcceptance.REJECTED,
                ),
            ),
        )

        // when
        val diagnostics = diagnosticsOf(strictLinter, printOf(number("42")))

        // then
        assertIs<Diagnostic.UnsupportedPrintlnArgument>(
            diagnostics.single(),
        )
    }

    /**
     * Una clase sin cubrir es un error de configuración y frena al
     * construir el linter, no en medio del análisis.
     */
    @Test
    fun `fails to build when the configuration leaves a kind uncovered`() {
        // given
        val incompleteConfiguration = RuleConfiguration.PrintlnArgument(
            acceptanceByKind = mapOf(
                ExpressionKind.LITERAL to ArgumentAcceptance.ACCEPTED,
            ),
        )

        // when
        val failure = assertFailsWith<IllegalArgumentException> {
            linterWith(incompleteConfiguration)
        }

        // then
        assertContains(
            charSequence = failure.message.orEmpty(),
            other = ExpressionKind.COMPOSED.name,
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
