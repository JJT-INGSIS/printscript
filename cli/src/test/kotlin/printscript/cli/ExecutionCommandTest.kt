package printscript.cli

import com.github.ajalt.clikt.testing.test
import printscript.cli.internal.command.ExecutionCommand
import printscript.cli.internal.io.Terminal
import printscript.cli.internal.operation.LanguageVersion
import printscript.cli.internal.operation.OperationOutcome
import printscript.cli.internal.operation.SourceOperation
import printscript.cli.internal.operation.SourceOperationFactory
import printscript.cli.internal.operation.SourceOperationRequest
import printscript.cli.internal.report.ErrorReporter
import printscript.statement.StatementSource
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Prueba la orquestación compartida por los cuatro comandos usando uno
 * real, con una [SourceOperationFactory] falsa.
 *
 * Antes esto se probaba con una subclase inventada de la clase base. Sin
 * clase base, el sujeto pasa a ser un comando de producción: lo único
 * sustituido es qué operación monta.
 */
class ExecutionCommandTest {

    private class FixedOutcomeOperation(
        private val outcome: OperationOutcome,
    ) : SourceOperation {

        override fun outcomeFor(statements: StatementSource, terminal: Terminal): OperationOutcome {
            terminal.writeLine("operación ejecutada")

            return outcome
        }
    }

    private class RecordingFactory(
        private val outcome: OperationOutcome = OperationOutcome.Success,
    ) : SourceOperationFactory {

        var receivedRequest: SourceOperationRequest? = null

        override fun create(request: SourceOperationRequest): SourceOperation {
            receivedRequest = request

            return FixedOutcomeOperation(outcome)
        }
    }

    private fun commandWith(factory: RecordingFactory) = ExecutionCommand(
        operationFactory = factory,
        errorReporter = ErrorReporter(),
    )

    private fun scriptFile(sourceCode: String = "let a: number = 5;"): String {
        val file = Files.createTempFile("printscript", ".ps")
        file.toFile().deleteOnExit()
        Files.writeString(file, sourceCode)

        return file.toString()
    }

    // --- los argumentos llegan a la factory -----------------------------

    @Test
    fun `hands the parsed arguments to the operation factory`() {
        val factory = RecordingFactory()
        val file = scriptFile()

        commandWith(factory).test(listOf(file, "--version", "1.0"))

        val request = assertNotNull(factory.receivedRequest)

        assertEquals(expected = file, actual = request.sourceFilePath.toString())
        assertEquals(expected = LanguageVersion.V1_0, actual = request.version)
    }

    @Test
    fun `defaults the version when it is not given`() {
        val factory = RecordingFactory()

        commandWith(factory).test(listOf(scriptFile()))

        assertEquals(
            expected = LanguageVersion.DEFAULT,
            actual = assertNotNull(factory.receivedRequest).version,
        )
    }

    @Test
    fun `rejects an unsupported version before building the operation`() {
        val factory = RecordingFactory()

        val result = commandWith(factory).test(listOf(scriptFile(), "--version", "9.9"))

        assertEquals(expected = false, actual = result.statusCode == 0)
        assertNull(factory.receivedRequest)
    }

    // --- cada resultado tiene su código de salida -----------------------

    @Test
    fun `exits with zero when the operation succeeds`() {
        val result = commandWith(RecordingFactory(OperationOutcome.Success)).test(listOf(scriptFile()))

        assertEquals(expected = 0, actual = result.statusCode)
        assertContains(result.stdout, "operación ejecutada")
    }

    @Test
    fun `exits with three when the operation reports findings`() {
        val result = commandWith(
            RecordingFactory(OperationOutcome.CompletedWithFindings("Se encontraron 2 problemas.")),
        ).test(listOf(scriptFile()))

        assertEquals(expected = 3, actual = result.statusCode)
        assertContains(result.stdout, "Se encontraron 2 problemas.")
    }

    @Test
    fun `exits with one when the operation fails`() {
        val result = commandWith(
            RecordingFactory(OperationOutcome.Failure("error: algo salió mal")),
        ).test(listOf(scriptFile()))

        assertEquals(expected = 1, actual = result.statusCode)
        assertContains(result.stderr, "algo salió mal")
    }

    // --- los mensajes siguen siendo nuestros ----------------------------

    @Test
    fun `reports a missing file with our own wording`() {
        val result = commandWith(RecordingFactory()).test(listOf("/no/existe/archivo.ps"))

        assertEquals(expected = 1, actual = result.statusCode)
        assertContains(result.stderr, "no se encontró el archivo")
    }

    // --- la ayuda -------------------------------------------------------

    @Test
    fun `generates a help page describing the argument and the version`() {
        val result = commandWith(RecordingFactory()).test("--help")

        assertContains(result.stdout, "archivo")
        assertContains(result.stdout, "--version")
    }

    @Test
    fun `no longer offers a configuration option`() {
        val result = commandWith(RecordingFactory()).test("--help")

        assertEquals(expected = false, actual = result.stdout.contains("--config"))
    }
}
