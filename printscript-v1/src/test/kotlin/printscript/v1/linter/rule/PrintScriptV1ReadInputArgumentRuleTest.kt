package printscript.v1.linter.rule

import printscript.ast.DeclaredType
import printscript.linter.Linter
import printscript.v1.linter.PrintScriptV11Diagnostic
import printscript.v1.linter.assertNoDiagnostics
import printscript.v1.linter.assign
import printscript.v1.linter.declare
import printscript.v1.linter.diagnosticsOf
import printscript.v1.linter.grouped
import printscript.v1.linter.negated
import printscript.v1.linter.number
import printscript.v1.linter.printOf
import printscript.v1.linter.readEnv
import printscript.v1.linter.readInput
import printscript.v1.linter.readInputArgumentRule
import printscript.v1.linter.sum
import printscript.v1.linter.text
import printscript.v1.linter.v11LinterWith
import printscript.v1.linter.variable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class PrintScriptV1ReadInputArgumentRuleTest {

    private val linter: Linter = v11LinterWith(rules = listOf(readInputArgumentRule()))

    @Test
    fun `accepts a string literal prompt`() {
        val diagnostics = diagnosticsOf(
            linter,
            declare("input", DeclaredType.STRING, readInput(text("Enter"))),
        )

        diagnostics.assertNoDiagnostics()
    }

    @Test
    fun `accepts a number literal prompt`() {
        val diagnostics = diagnosticsOf(
            linter,
            declare("input", DeclaredType.STRING, readInput(number("1"))),
        )

        diagnostics.assertNoDiagnostics()
    }

    @Test
    fun `accepts a variable prompt`() {
        val diagnostics = diagnosticsOf(
            linter,
            declare("input", DeclaredType.STRING, readInput(variable("message"))),
        )

        diagnostics.assertNoDiagnostics()
    }

    @Test
    fun `rejects a binary expression prompt`() {
        val argument = readInput(sum(text("Enter"), text("something")))

        val diagnostics = diagnosticsOf(linter, declare("input", DeclaredType.STRING, argument))

        val violation = assertIs<PrintScriptV11Diagnostic.UnsupportedReadInputArgument>(
            diagnostics.single(),
        )
        assertEquals(
            expected = sum(text("Enter"), text("something")),
            actual = violation.argument,
        )
    }

    @Test
    fun `rejects a unary expression prompt`() {
        val diagnostics = diagnosticsOf(
            linter,
            declare("input", DeclaredType.STRING, readInput(negated(number("1")))),
        )

        assertIs<PrintScriptV11Diagnostic.UnsupportedReadInputArgument>(diagnostics.single())
    }

    @Test
    fun `rejects a grouped expression prompt`() {
        val diagnostics = diagnosticsOf(
            linter,
            declare("input", DeclaredType.STRING, readInput(grouped(text("Enter")))),
        )

        assertIs<PrintScriptV11Diagnostic.UnsupportedReadInputArgument>(diagnostics.single())
    }

    @Test
    fun `finds a readInput inside an assignment`() {
        val diagnostics = diagnosticsOf(
            linter,
            assign("input", readInput(sum(text("a"), text("b")))),
        )

        assertIs<PrintScriptV11Diagnostic.UnsupportedReadInputArgument>(diagnostics.single())
    }

    @Test
    fun `finds a readInput inside a println argument`() {
        val diagnostics = diagnosticsOf(
            linter,
            printOf(readInput(sum(text("a"), text("b")))),
        )

        assertIs<PrintScriptV11Diagnostic.UnsupportedReadInputArgument>(diagnostics.single())
    }

    @Test
    fun `finds a readInput nested inside a binary expression`() {
        val diagnostics = diagnosticsOf(
            linter,
            declare(
                "input",
                DeclaredType.STRING,
                sum(readInput(sum(text("a"), text("b"))), text("!")),
            ),
        )

        assertIs<PrintScriptV11Diagnostic.UnsupportedReadInputArgument>(diagnostics.single())
    }

    @Test
    fun `does not confuse readEnv with readInput`() {
        val diagnostics = diagnosticsOf(
            linter,
            declare("input", DeclaredType.STRING, readEnv(sum(text("A"), text("B")))),
        )

        diagnostics.assertNoDiagnostics()
    }

    @Test
    fun `ignores statements without expressions`() {
        val diagnostics = diagnosticsOf(linter)

        diagnostics.assertNoDiagnostics()
    }
}
