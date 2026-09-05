package printscript.v1.linter

import printscript.ast.DeclaredType
import kotlin.test.Test
import kotlin.test.assertIs

class PrintScriptV11LinterFactoryTest {

    @Test
    fun `default configuration reuses the V1 defaults`() {
        val linter = PrintScriptV11LinterFactory.create()

        val diagnostics = diagnosticsOf(
            linter,
            declare("my_total", DeclaredType.NUMBER, number("1")),
            printOf(sum(number("2"), number("3"))),
        )

        assertIs<PrintScriptV1Diagnostic.NamingConventionViolation>(diagnostics.first())
        assertIs<PrintScriptV1Diagnostic.UnsupportedPrintlnArgument>(diagnostics.last())
    }

    @Test
    fun `default configuration does not enable the readInput restriction`() {
        val linter = PrintScriptV11LinterFactory.create()

        val diagnostics = diagnosticsOf(
            linter,
            declare("input", DeclaredType.STRING, readInput(sum(text("a"), text("b")))),
        )

        diagnostics.assertNoDiagnostics()
    }

    @Test
    fun `does not confuse readEnv with readInput by default`() {
        val linter = v11LinterWith(rules = listOf(readInputArgumentRule()))

        val diagnostics = diagnosticsOf(
            linter,
            declare("value", DeclaredType.STRING, readEnv(sum(text("A"), text("B")))),
        )

        diagnostics.assertNoDiagnostics()
    }
}
