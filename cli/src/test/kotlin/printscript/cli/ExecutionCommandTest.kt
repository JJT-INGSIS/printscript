package printscript.cli

import com.github.ajalt.clikt.testing.test
import printscript.cli.internal.command.ExecutionCommand
import printscript.cli.internal.report.ErrorReporter
import printscript.cli.internal.toolchain.LanguageVersion
import printscript.cli.internal.toolchain.PrintScriptToolchain
import printscript.interpreter.InterpretationResult
import printscript.interpreter.Interpreter
import printscript.interpreter.SemanticError
import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan
import printscript.parser.Parser
import printscript.runtime.ProgramOutput
import printscript.statement.StatementReadResult
import printscript.statement.StatementSource
import printscript.token.TokenSource
import printscript.v1.lexer.PrintScriptV1LexerFactory
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class ExecutionCommandTest {

    private val anySpan = SourceSpan(
        start = SourcePosition(line = 1, column = 1, offset = 0),
        end = SourcePosition(line = 1, column = 2, offset = 1),
    )

    private object EmptyStatementSource : StatementSource {

        override fun nextStatement(): StatementReadResult = StatementReadResult.EndOfInput
    }

    private object EmptyParser : Parser {

        override fun parse(tokens: TokenSource): StatementSource = EmptyStatementSource
    }

    private class FixedResultInterpreter(
        private val result: InterpretationResult,
        private val output: ProgramOutput,
        private val printedLine: String?,
    ) : Interpreter {

        override fun interpret(source: StatementSource): InterpretationResult {
            printedLine?.let(output::writeLine)

            return result
        }
    }

    private class RecordingToolchainFactory(
        private val result: InterpretationResult = InterpretationResult.Success,
        private val printedLine: String? = null,
    ) : (LanguageVersion) -> PrintScriptToolchain {

        var receivedVersion: LanguageVersion? = null

        override fun invoke(version: LanguageVersion): PrintScriptToolchain {
            receivedVersion = version

            return PrintScriptToolchain(
                lexer = PrintScriptV1LexerFactory.create(),
                parser = EmptyParser,
                interpreterUsing = { environment ->
                    FixedResultInterpreter(
                        result = result,
                        output = environment.output,
                        printedLine = printedLine,
                    )
                },
                validator = { error("ExecutionCommand no debería pedir el validador") },
                formatterConfiguredBy = {
                    error("ExecutionCommand no debería pedir el formatter")
                },
                linterConfiguredBy = {
                    error("ExecutionCommand no debería pedir el linter")
                },
            )
        }
    }

    private fun commandWith(factory: RecordingToolchainFactory) = ExecutionCommand(
        errorReporter = ErrorReporter(),
        toolchainFor = factory,
    )

    private fun anySourceFile(): String {
        return scriptFile("let a: number = 5;")
    }

    @Test
    fun `asks the toolchain for the requested version`() {
        val factory = RecordingToolchainFactory()

        commandWith(factory).test(listOf(anySourceFile(), "--version", "1.0"))

        assertEquals(expected = LanguageVersion.V1_0, actual = factory.receivedVersion)
    }

    @Test
    fun `accepts version 1_1`() {
        val factory = RecordingToolchainFactory()

        commandWith(factory).test(listOf(anySourceFile(), "--version", "1.1"))

        assertEquals(expected = LanguageVersion.V1_1, actual = factory.receivedVersion)
    }

    @Test
    fun `defaults the version when it is not given`() {
        val factory = RecordingToolchainFactory()

        commandWith(factory).test(listOf(anySourceFile()))

        assertEquals(expected = LanguageVersion.DEFAULT, actual = factory.receivedVersion)
    }

    @Test
    fun `rejects an unsupported version before asking for a toolchain`() {
        val factory = RecordingToolchainFactory()

        val result = commandWith(factory).test(listOf(anySourceFile(), "--version", "9.9"))

        assertNotEquals(illegal = 0, actual = result.statusCode)
        assertNull(factory.receivedVersion)
    }

    @Test
    fun `prints what the program writes`() {
        val factory = RecordingToolchainFactory(printedLine = "hola mundo")

        val result = commandWith(factory).test(listOf(anySourceFile()))

        assertEquals(expected = 0, actual = result.statusCode)
        assertContains(result.stdout, "hola mundo")
    }

    @Test
    fun `exits with zero when interpretation succeeds`() {
        val result = commandWith(RecordingToolchainFactory()).test(listOf(anySourceFile()))

        assertEquals(expected = 0, actual = result.statusCode)
    }

    @Test
    fun `exits with one when interpretation fails`() {
        val factory = RecordingToolchainFactory(
            result = InterpretationResult.SemanticFailure(
                SemanticError.UnsupportedStatement(span = anySpan),
            ),
        )

        val result = commandWith(factory).test(listOf(anySourceFile()))

        assertEquals(expected = 1, actual = result.statusCode)
        assertContains(result.stderr, "error:")
    }

    @Test
    fun `reports a missing file with our own wording`() {
        val result = commandWith(RecordingToolchainFactory()).test(listOf("/no/existe/archivo.ps"))

        assertEquals(expected = 1, actual = result.statusCode)
        assertContains(result.stderr, "no se encontró el archivo")
    }

    @Test
    fun `reports a directory instead of trying to read it`() {
        val directory = Files.createTempDirectory("printscript-source")
        directory.toFile().deleteOnExit()

        val result = commandWith(RecordingToolchainFactory()).test(listOf(directory.toString()))

        assertEquals(expected = 1, actual = result.statusCode)
        assertContains(result.stderr, "no es un archivo")
    }

    @Test
    fun `closes the source file when the operation does not consume it`() {
        val sourceFilePath = Path.of(anySourceFile())

        val result = commandWith(RecordingToolchainFactory()).test(listOf(sourceFilePath.toString()))
        Files.delete(sourceFilePath)

        assertEquals(expected = 0, actual = result.statusCode)
        assertFalse(Files.exists(sourceFilePath))
    }

    @Test
    fun `generates a help page describing the argument and the version`() {
        val result = commandWith(RecordingToolchainFactory()).test("--help")

        assertContains(result.stdout, "archivo")
        assertContains(result.stdout, "--version")
    }

    @Test
    fun `no longer offers a configuration option`() {
        val result = commandWith(RecordingToolchainFactory()).test("--help")

        assertFalse(result.stdout.contains("--config"))
    }
}
