package printscript.cli

import printscript.cli.internal.CliApplication
import printscript.cli.internal.ExitCode
import printscript.cli.internal.arguments.CliArgumentsParser
import printscript.cli.internal.command.CommandDispatcher
import printscript.cli.internal.command.ExecutionCommand
import printscript.cli.internal.command.ValidationCommand
import printscript.cli.internal.pipeline.StatementSourcePipeline
import printscript.cli.internal.report.ErrorReporter
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CliApplicationTest {

    private val terminal = RecordingTerminal()

    private val application = CliApplication(
        terminal = terminal,
        argumentsParser = CliArgumentsParser(),
        pipeline = StatementSourcePipeline(),
        errorReporter = ErrorReporter(),
        commandDispatcher = CommandDispatcher(
            commands = listOf(
                ValidationCommand(ErrorReporter()),
                ExecutionCommand(ErrorReporter()),
            ),
        ),
    )

    private fun scriptFile(sourceCode: String): String {
        val file = Files.createTempFile("printscript", ".ps")
        file.toFile().deleteOnExit()
        Files.writeString(file, sourceCode)

        return file.toString()
    }

    // --- execution ---------------------------------------------------

    @Test
    fun `executes a valid program and prints its output`() {
        val file = scriptFile(
            """
            let name: string = "Joe";
            println(name + " Doe");
            """.trimIndent(),
        )

        val exitCode = application.runCommandLine(listOf("execution", file))

        assertEquals(expected = ExitCode.SUCCESS, actual = exitCode)

        assertEquals(
            expected = listOf("Joe Doe"),
            actual = terminal.output(),
        )
    }

    @Test
    fun `evaluates arithmetic respecting precedence`() {
        val file = scriptFile("println(2 + 3 * 4);")

        application.runCommandLine(listOf("execution", file))

        assertEquals(
            expected = listOf("14"),
            actual = terminal.output(),
        )
    }

    // --- validation --------------------------------------------------

    @Test
    fun `validation accepts a well formed program`() {
        val file = scriptFile("let a: number = 5;")

        val exitCode = application.runCommandLine(listOf("validation", file))

        assertEquals(expected = ExitCode.SUCCESS, actual = exitCode)
        assertEquals(expected = listOf("El archivo es válido."), actual = terminal.output())
    }

    @Test
    fun `validation rejects an undeclared variable`() {
        val file = scriptFile("println(inexistente);")

        val exitCode = application.runCommandLine(listOf("validation", file))

        assertEquals(expected = ExitCode.SOURCE_ERROR, actual = exitCode)

        assertContains(terminal.errors().last(), "'inexistente' no fue declarada")
    }

    @Test
    fun `validation rejects a type mismatch`() {
        val file = scriptFile("""let a: number = "hola";""")

        val exitCode = application.runCommandLine(listOf("validation", file))

        assertEquals(expected = ExitCode.SOURCE_ERROR, actual = exitCode)

        assertContains(terminal.errors().last(), "es de tipo number")
    }

    @Test
    fun `validation does not print what the program would print`() {
        val file = scriptFile("""println("no deberia verse");""")

        application.runCommandLine(listOf("validation", file))

        assertEquals(
            expected = listOf("El archivo es válido."),
            actual = terminal.output(),
        )
    }

    // --- errores -----------------------------------------------------

    @Test
    fun `reports a syntax error with its position`() {
        val file = scriptFile("let a: number = 5")

        val exitCode = application.runCommandLine(listOf("validation", file))

        assertEquals(expected = ExitCode.SOURCE_ERROR, actual = exitCode)

        val message = terminal.errors().last()

        assertContains(message, "error:")
        assertContains(message, "línea 1")
    }

    @Test
    fun `a failing program prints nothing before the error`() {
        val file = scriptFile("println(inexistente);")

        application.runCommandLine(listOf("execution", file))

        assertEquals(expected = emptyList(), actual = terminal.output())
    }

    @Test
    fun `reports division by zero`() {
        val file = scriptFile("println(1 / 0);")

        val exitCode = application.runCommandLine(listOf("execution", file))

        assertEquals(expected = ExitCode.SOURCE_ERROR, actual = exitCode)
        assertContains(terminal.errors().last(), "división por cero")
    }

    // --- uso ---------------------------------------------------------

    @Test
    fun `rejects an unknown operation listing the available ones`() {
        val file = scriptFile("let a: number = 5;")

        val exitCode = application.runCommandLine(listOf("dancing", file))

        assertEquals(expected = ExitCode.USAGE_ERROR, actual = exitCode)
        assertContains(terminal.errors().first(), "validation")
    }

    @Test
    fun `rejects a call without a source file`() {
        val exitCode = application.runCommandLine(listOf("validation"))

        assertEquals(expected = ExitCode.USAGE_ERROR, actual = exitCode)
        assertContains(terminal.errors().first(), "Faltan argumentos")
    }

    @Test
    fun `reports a missing file without touching the pipeline`() {
        val exitCode = application.runCommandLine(
            listOf("validation", "/no/existe/archivo.ps"),
        )

        assertEquals(expected = ExitCode.SOURCE_ERROR, actual = exitCode)
        assertContains(terminal.errors().first(), "no se encontró el archivo")
    }

    @Test
    fun `accepts the version argument`() {
        val file = scriptFile("let a: number = 5;")

        assertEquals(
            expected = ExitCode.SUCCESS,
            actual = application.runCommandLine(listOf("validation", file, "1.0")),
        )
    }

    // --- salidas separadas -------------------------------------------

    @Test
    fun `progress goes to standard error, never to standard output`() {
        val file = scriptFile(
            """
            let a: number = 1;
            let b: number = 2;
            println(a + b);
            """.trimIndent(),
        )

        application.runCommandLine(listOf("execution", file))

        assertTrue(terminal.errors().any { line -> line.startsWith("parsing...") })
        assertTrue(terminal.output().none { line -> line.startsWith("parsing...") })
    }

    @Test
    fun `errors never reach standard output`() {
        val file = scriptFile("let a: number = 5")

        application.runCommandLine(listOf("validation", file))

        assertTrue(terminal.output().none { line -> line.contains("error:") })
    }
}
