package printscript.cli

import com.github.ajalt.clikt.testing.test
import printscript.cli.internal.command.SourceFileOperationCommand
import printscript.cli.internal.io.Terminal
import printscript.cli.internal.operation.LanguageVersion
import printscript.cli.internal.operation.OperationOutcome
import printscript.cli.internal.operation.SourceOperation
import printscript.cli.internal.operation.SourceOperationRequest
import printscript.statement.StatementSource
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SourceFileOperationCommandTest {

    private class FixedOutcomeOperation(
        private val outcome: OperationOutcome,
    ) : SourceOperation {

        override fun outcomeFor(statements: StatementSource, terminal: Terminal): OperationOutcome {
            terminal.writeLine("operación ejecutada")

            return outcome
        }
    }

    private class ProbeCommand(
        private val outcome: OperationOutcome = OperationOutcome.Success,
    ) : SourceFileOperationCommand(name = "probe") {

        var receivedRequest: SourceOperationRequest? = null

        override fun operationFor(request: SourceOperationRequest): SourceOperation {
            receivedRequest = request

            return FixedOutcomeOperation(outcome)
        }
    }

    private fun scriptFile(sourceCode: String = "let a: number = 5;"): String {
        val file = Files.createTempFile("printscript", ".ps")
        file.toFile().deleteOnExit()
        Files.writeString(file, sourceCode)

        return file.toString()
    }

    // --- los argumentos llegan al subcomando concreto -------------------

    @Test
    fun `hands the parsed arguments to the concrete command`() {
        val command = ProbeCommand()
        val file = scriptFile()

        command.test("$file --version 1.0 --config reglas.json")

        val request = assertNotNull(command.receivedRequest)

        assertEquals(expected = file, actual = request.sourceFilePath.toString())
        assertEquals(expected = LanguageVersion.V1_0, actual = request.version)
        assertEquals(expected = "reglas.json", actual = request.configurationFilePath)
    }

    @Test
    fun `defaults the version when it is not given`() {
        val command = ProbeCommand()

        command.test(scriptFile())

        assertEquals(
            expected = LanguageVersion.DEFAULT,
            actual = assertNotNull(command.receivedRequest).version,
        )
    }

    @Test
    fun `has no configuration file when none is given`() {
        val command = ProbeCommand()

        command.test(scriptFile())

        assertNull(assertNotNull(command.receivedRequest).configurationFilePath)
    }

    @Test
    fun `rejects an unsupported version before running the operation`() {
        val command = ProbeCommand()

        val result = command.test("${scriptFile()} --version 9.9")

        assertEquals(expected = false, actual = result.statusCode == 0)
        assertNull(command.receivedRequest)
    }

    // --- cada resultado tiene su código de salida -----------------------

    @Test
    fun `exits with zero when the operation succeeds`() {
        val result = ProbeCommand(OperationOutcome.Success).test(scriptFile())

        assertEquals(expected = 0, actual = result.statusCode)
        assertContains(result.stdout, "operación ejecutada")
    }

    @Test
    fun `exits with three when the operation reports findings`() {
        val result = ProbeCommand(
            OperationOutcome.CompletedWithFindings("Se encontraron 2 problemas."),
        ).test(scriptFile())

        assertEquals(expected = 3, actual = result.statusCode)
        assertContains(result.stdout, "Se encontraron 2 problemas.")
    }

    @Test
    fun `exits with one when the operation fails`() {
        val result = ProbeCommand(
            OperationOutcome.Failure("error: algo salió mal"),
        ).test(scriptFile())

        assertEquals(expected = 1, actual = result.statusCode)
        assertContains(result.stderr, "algo salió mal")
    }

    // --- los mensajes siguen siendo nuestros ----------------------------

    @Test
    fun `reports a missing file with our own wording`() {
        val result = ProbeCommand().test("/no/existe/archivo.ps")

        assertEquals(expected = 1, actual = result.statusCode)
        assertContains(result.stderr, "no se encontró el archivo")
    }

    @Test
    fun `generates a help page describing the argument and the options`() {
        val result = ProbeCommand().test("--help")

        assertContains(result.stdout, "<archivo>")
        assertContains(result.stdout, "--version")
        assertContains(result.stdout, "--config")
    }
}
