package printscript.v1.linter

import printscript.ast.DeclaredType
import printscript.linter.Linter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class PrintScriptV1IdentifierNamingRuleTest {

    private fun linterFor(convention: PrintScriptV1NamingConvention): Linter {
        return linterWith(
            PrintScriptV1RuleConfiguration.IdentifierNaming(convention),
        )
    }

    @Test
    fun `accepts a declaration written in camel case`() {
        // given
        val linter = linterFor(PrintScriptV1NamingConvention.CAMEL_CASE)
        val declaration = declare(
            variableName = "myVariable",
            type = DeclaredType.NUMBER,
            initializer = number("1"),
        )

        // when
        val diagnostics = diagnosticsOf(linter, declaration)

        // then
        diagnostics.assertNoDiagnostics()
    }

    @Test
    fun `reports a snake case declaration when camel case is expected`() {
        // given
        val linter = linterFor(PrintScriptV1NamingConvention.CAMEL_CASE)
        val declaration = declare(
            variableName = "my_variable",
            type = DeclaredType.NUMBER,
            initializer = number("1"),
        )

        // when
        val diagnostics = diagnosticsOf(linter, declaration)

        // then
        diagnostics.assertNamingViolations("my_variable")
    }

    @Test
    fun `accepts a declaration written in snake case`() {
        // given
        val linter = linterFor(PrintScriptV1NamingConvention.SNAKE_CASE)
        val declaration = declare(
            variableName = "my_variable",
            type = DeclaredType.NUMBER,
            initializer = number("1"),
        )

        // when
        val diagnostics = diagnosticsOf(linter, declaration)

        // then
        diagnostics.assertNoDiagnostics()
    }

    @Test
    fun `reports a camel case declaration when snake case is expected`() {
        // given
        val linter = linterFor(PrintScriptV1NamingConvention.SNAKE_CASE)
        val declaration = declare(
            variableName = "myVariable",
            type = DeclaredType.NUMBER,
            initializer = number("1"),
        )

        // when
        val diagnostics = diagnosticsOf(linter, declaration)

        // then
        diagnostics.assertNamingViolations("myVariable")
    }

    @Test
    fun `reports every offending declaration in order`() {
        // given
        val linter = linterFor(PrintScriptV1NamingConvention.CAMEL_CASE)

        // when
        val diagnostics = diagnosticsOf(
            linter,
            declare("first_one", DeclaredType.NUMBER, number("1")),
            declare("secondOne", DeclaredType.NUMBER, number("2")),
            declare("third_one", DeclaredType.NUMBER, number("3")),
        )

        // then
        diagnostics.assertNamingViolations("first_one", "third_one")
    }

    /**
     * Revisar también el destino de una asignación marcaría dos veces el
     * mismo nombre mal escrito.
     */
    @Test
    fun `ignores identifiers outside their declaration`() {
        // given
        val linter = linterFor(PrintScriptV1NamingConvention.CAMEL_CASE)

        // when
        val diagnostics = diagnosticsOf(
            linter,
            assign("my_variable", number("1")),
            printOf(variable("other_variable")),
        )

        // then
        diagnostics.assertNoDiagnostics()
    }

    @Test
    fun `reports which convention was expected`() {
        // given
        val linter = linterFor(PrintScriptV1NamingConvention.SNAKE_CASE)
        val declaration = declare(
            variableName = "myVariable",
            type = DeclaredType.NUMBER,
            initializer = number("1"),
        )

        // when
        val diagnostics = diagnosticsOf(linter, declaration)

        // then
        val violation = assertIs<PrintScriptV1Diagnostic.NamingConventionViolation>(
            diagnostics.single(),
        )

        assertEquals(
            expected = PrintScriptV1NamingConvention.SNAKE_CASE,
            actual = violation.expectedConvention,
        )
    }
}
