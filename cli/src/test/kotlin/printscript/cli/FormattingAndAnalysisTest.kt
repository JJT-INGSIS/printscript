package printscript.cli

import printscript.cli.internal.CliApplication
import printscript.cli.internal.ExitCode
import printscript.cli.internal.arguments.CliArgumentsParser
import printscript.cli.internal.command.AnalysisCommand
import printscript.cli.internal.command.CommandDispatcher
import printscript.cli.internal.command.FormattingCommand
import printscript.cli.internal.pipeline.StatementSourcePipeline
import printscript.cli.internal.report.DiagnosticReporter
import printscript.cli.internal.report.ErrorReporter
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FormattingAndAnalysisTest {

    private val terminal = RecordingTerminal()

    private val application = CliApplication(
        terminal = terminal,
        argumentsParser = CliArgumentsParser(),
        pipeline = StatementSourcePipeline(),
        errorReporter = ErrorReporter(),
        commandDispatcher = CommandDispatcher(
            commands = listOf(
                FormattingCommand(ErrorReporter()),
                AnalysisCommand(ErrorReporter(), DiagnosticReporter()),
            ),
        ),
    )

    private fun scriptFile(sourceCode: String): String {
        val file = Files.createTempFile("printscript", ".ps")
        file.toFile().deleteOnExit()
        Files.writeString(file, sourceCode)

        return file.toString()
    }

    // --- formatting --------------------------------------------------

    @Test
    fun `normalizes spacing in a declaration`() {
        val file = scriptFile("let    a:number   =   5;")

        val exitCode = application.runCommandLine(listOf("formatting", file))

        assertEquals(expected = ExitCode.SUCCESS, actual = exitCode)
        assertContains(terminal.outputText(), "let a: number = 5;")
    }

    @Test
    fun `keeps the original quote style`() {
        val file = scriptFile("""let name:string='Joe';""")

        application.runCommandLine(listOf("formatting", file))

        assertContains(terminal.outputText(), "'Joe'")
    }

    @Test
    fun `formatting reports a syntax error instead of writing code`() {
        val file = scriptFile("let a: number = 5")

        val exitCode = application.runCommandLine(listOf("formatting", file))

        assertEquals(expected = ExitCode.SOURCE_ERROR, actual = exitCode)
        assertContains(terminal.errors().last(), "error:")
    }

    // --- analyzing ---------------------------------------------------

    @Test
    fun `accepts a program that respects the conventions`() {
        val file = scriptFile(
            """
            let miVariable: number = 5;
            println(miVariable);
            """.trimIndent(),
        )

        val exitCode = application.runCommandLine(listOf("analyzing", file))

        assertEquals(expected = ExitCode.SUCCESS, actual = exitCode)
        assertContains(terminal.outputText(), "No se encontraron problemas.")
    }

    @Test
    fun `reports an identifier that is not camel case`() {
        val file = scriptFile("let mi_variable: number = 5;")

        val exitCode = application.runCommandLine(listOf("analyzing", file))

        assertEquals(expected = ExitCode.FINDINGS, actual = exitCode)

        val message = terminal.output().first()

        assertContains(message, "aviso:")
        assertContains(message, "mi_variable")
        assertContains(message, "camelCase")
    }

    @Test
    fun `reports a println called with an expression`() {
        val file = scriptFile("println(1 + 2);")

        application.runCommandLine(listOf("analyzing", file))

        assertContains(terminal.output().first(), "println no acepta una expresión")
    }

    @Test
    fun `reports every finding instead of stopping at the first`() {
        val file = scriptFile(
            """
            let mi_variable: number = 5;
            let otra_variable: number = 6;
            println(mi_variable + otra_variable);
            """.trimIndent(),
        )

        application.runCommandLine(listOf("analyzing", file))

        assertTrue(terminal.output().count { line -> line.startsWith("aviso:") } >= 3)
    }

    @Test
    fun `findings are warnings and never errors`() {
        val file = scriptFile("let mi_variable: number = 5;")

        application.runCommandLine(listOf("analyzing", file))

        assertTrue(terminal.output().none { line -> line.startsWith("error:") })
        assertTrue(terminal.errors().none { line -> line.startsWith("error:") })
    }

    @Test
    fun `a file with findings exits differently than a broken file`() {
        val withFindings = scriptFile("let mi_variable: number = 5;")
        val broken = scriptFile("let a: number = 5")

        assertEquals(
            expected = ExitCode.FINDINGS,
            actual = application.runCommandLine(listOf("analyzing", withFindings)),
        )

        assertEquals(
            expected = ExitCode.SOURCE_ERROR,
            actual = application.runCommandLine(listOf("analyzing", broken)),
        )
    }
}
