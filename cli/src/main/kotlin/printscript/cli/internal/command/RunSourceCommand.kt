package printscript.cli.internal.command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.mordant.terminal.prompt
import printscript.cli.internal.ExitCode
import printscript.cli.internal.OperationOutcome
import printscript.cli.internal.report.ErrorReporter
import printscript.cli.internal.toolchain.ConfiguredToolResult
import printscript.interpreter.InterpretationResult
import printscript.interpreter.Interpreter
import printscript.runtime.EnvironmentVariableProvider
import printscript.runtime.ProgramInput
import printscript.source.SourceReader
import printscript.source.SourceReaderCreationResult
import printscript.source.SourceReaderFactory
import printscript.statement.StatementSource
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

internal fun CliktCommand.runOnSourceFile(
    sourceFilePath: Path,
    errorReporter: ErrorReporter,
    outcomeFrom: (SourceReader) -> OperationOutcome,
) {
    val outcome = when (val creation = SourceReaderFactory.fromPath(sourceFilePath)) {
        is SourceReaderCreationResult.Failure ->
            OperationOutcome.Failure(errorReporter.describe(creation.error))

        is SourceReaderCreationResult.Success ->
            outcomeFrom(creation.reader)
    }

    reportOutcome(outcome)

    val exitCode = exitCodeOf(outcome)

    if (exitCode != ExitCode.SUCCESS) {
        throw ProgramResult(exitCode.value)
    }
}

internal fun interpretationOutcome(
    interpreter: Interpreter,
    statements: StatementSource,
    errorReporter: ErrorReporter,
    onSuccess: () -> Unit = { },
): OperationOutcome {
    return when (val result = interpreter.interpret(statements)) {
        InterpretationResult.Success -> {
            onSuccess()

            OperationOutcome.Success
        }

        is InterpretationResult.ParseFailure ->
            OperationOutcome.Failure(
                errorReporter.describe(result.error),
            )

        is InterpretationResult.SemanticFailure ->
            OperationOutcome.Failure(
                errorReporter.describe(result.error),
            )
    }
}

internal fun <T> CliktCommand.configuredToolFrom(
    configurationFilePath: Path?,
    toolConfiguredBy: (String?) -> ConfiguredToolResult<T>,
): T {
    val configuration = configurationFilePath?.let { path -> readConfiguration(path) }

    return when (val result = toolConfiguredBy(configuration)) {
        is ConfiguredToolResult.Success -> result.tool
        is ConfiguredToolResult.Failure -> failWith(result.reason)
    }
}

internal fun CliktCommand.terminalInput(): ProgramInput {
    return ProgramInput { message ->
        terminal.prompt(
            prompt = message,
            promptSuffix = "",
        )
    }
}

internal fun systemEnvironmentVariables(): EnvironmentVariableProvider {
    return EnvironmentVariableProvider { name -> System.getenv(name) }
}

private fun CliktCommand.readConfiguration(path: Path): String {
    return try {
        Files.readString(path, StandardCharsets.UTF_8)
    } catch (error: IOException) {
        failWith("no se pudo leer la configuración '$path': ${error.message}")
    }
}

private fun CliktCommand.failWith(description: String): Nothing {
    echo("error: $description", err = true)
    throw ProgramResult(ExitCode.SOURCE_ERROR.value)
}

private fun CliktCommand.reportOutcome(outcome: OperationOutcome) {
    when (outcome) {
        OperationOutcome.Success -> Unit

        is OperationOutcome.CompletedWithFindings -> echo(outcome.message)

        is OperationOutcome.Failure -> echo(outcome.message, err = true)
    }
}

private fun exitCodeOf(outcome: OperationOutcome): ExitCode {
    return when (outcome) {
        OperationOutcome.Success -> ExitCode.SUCCESS

        is OperationOutcome.CompletedWithFindings -> ExitCode.FINDINGS

        is OperationOutcome.Failure -> ExitCode.SOURCE_ERROR
    }
}
