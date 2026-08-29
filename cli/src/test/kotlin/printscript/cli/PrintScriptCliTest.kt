package printscript.cli

import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.testing.test
import printscript.cli.internal.command.AnalysisCommand
import printscript.cli.internal.command.ExecutionCommand
import printscript.cli.internal.command.FormattingCommand
import printscript.cli.internal.command.PrintScriptCommandGroup
import printscript.cli.internal.command.ValidationCommand
import printscript.cli.internal.report.DiagnosticReporter
import printscript.cli.internal.report.ErrorReporter
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrintScriptCliTest {

    private fun printScriptCli() = PrintScriptCommandGroup().subcommands(
        ValidationCommand(ErrorReporter()),
        ExecutionCommand(ErrorReporter()),
        FormattingCommand(ErrorReporter()),
        AnalysisCommand(ErrorReporter(), DiagnosticReporter()),
    )

    private fun scriptFile(sourceCode: String): String {
        val file = Files.createTempFile("printscript", ".ps")
        file.toFile().deleteOnExit()
        Files.writeString(file, sourceCode)

        return file.toString()
    }

    // --- execution -----------------------------------------------------

    @Test
    fun `executes a valid program and prints its output`() {
        val file = scriptFile(
            """
            let name: string = "Joe";
            println(name + " Doe");
            """.trimIndent(),
        )

        val result = printScriptCli().test("execution $file")

        assertEquals(expected = 0, actual = result.statusCode)
        assertContains(result.stdout, "Joe Doe")
    }

    @Test
    fun `evaluates arithmetic respecting precedence`() {
        val file = scriptFile("println(2 + 3 * 4);")

        assertContains(printScriptCli().test("execution $file").stdout, "14")
    }

    @Test
    fun `reports division by zero`() {
        val file = scriptFile("println(1 / 0);")

        val result = printScriptCli().test("execution $file")

        assertEquals(expected = 1, actual = result.statusCode)
        assertContains(result.stderr, "división por cero")
    }

    // --- validation ----------------------------------------------------

    @Test
    fun `validation accepts a well formed program`() {
        val file = scriptFile("let a: number = 5;")

        val result = printScriptCli().test("validation $file")

        assertEquals(expected = 0, actual = result.statusCode)
        assertContains(result.stdout, "El archivo es válido.")
    }

    @Test
    fun `validation does not print what the program would print`() {
        val file = scriptFile("""println("no deberia verse");""")

        val result = printScriptCli().test("validation $file")

        assertTrue(!result.stdout.contains("no deberia verse"))
    }

    @Test
    fun `validation rejects an undeclared variable`() {
        val file = scriptFile("println(inexistente);")

        val result = printScriptCli().test("validation $file")

        assertEquals(expected = 1, actual = result.statusCode)
        assertContains(result.stderr, "'inexistente' no fue declarada")
    }

    @Test
    fun `reports a syntax error with its position`() {
        val file = scriptFile("let a: number = 5")

        val result = printScriptCli().test("validation $file")

        assertEquals(expected = 1, actual = result.statusCode)
        assertContains(result.stderr, "línea 1")
    }

    // --- formatting ----------------------------------------------------

    @Test
    fun `normalizes spacing in a declaration`() {
        val file = scriptFile("let    a:number   =   5;")

        val result = printScriptCli().test("formatting $file")

        assertEquals(expected = 0, actual = result.statusCode)
        assertContains(result.stdout, "let a: number = 5;")
    }

    // --- analyzing -----------------------------------------------------

    @Test
    fun `accepts a program that respects the conventions`() {
        val file = scriptFile("let miVariable: number = 5;")

        val result = printScriptCli().test("analyzing $file")

        assertEquals(expected = 0, actual = result.statusCode)
        assertContains(result.stdout, "No se encontraron problemas.")
    }

    @Test
    fun `reports an identifier that is not camel case`() {
        val file = scriptFile("let mi_variable: number = 5;")

        val result = printScriptCli().test("analyzing $file")

        assertEquals(expected = 3, actual = result.statusCode)
        assertContains(result.stdout, "camelCase")
    }

    @Test
    fun `a file with findings exits differently than a broken file`() {
        val withFindings = scriptFile("let mi_variable: number = 5;")
        val broken = scriptFile("let a: number = 5")

        assertEquals(
            expected = 3,
            actual = printScriptCli().test("analyzing $withFindings").statusCode,
        )

        assertEquals(
            expected = 1,
            actual = printScriptCli().test("analyzing $broken").statusCode,
        )
    }

    // --- errores de uso, ahora los reporta Clikt ------------------------

    @Test
    fun `rejects an unknown operation`() {
        val result = printScriptCli().test("dancing archivo.ps")

        assertTrue(result.statusCode != 0)
    }

    @Test
    fun `rejects a call without a source file`() {
        val result = printScriptCli().test("validation")

        assertTrue(result.statusCode != 0)
        assertEquals(expected = "", actual = result.stdout)
    }

    @Test
    fun `reports a missing file with our own wording`() {
        val result = printScriptCli().test("validation /no/existe/archivo.ps")

        assertEquals(expected = 1, actual = result.statusCode)
        assertContains(result.stderr, "no se encontró el archivo")
    }

    // --- ayuda ----------------------------------------------------------

    @Test
    fun `lists the four operations in the help page`() {
        val result = printScriptCli().test("--help")

        assertContains(result.stdout, "validation")
        assertContains(result.stdout, "execution")
        assertContains(result.stdout, "formatting")
        assertContains(result.stdout, "analyzing")
    }
}
