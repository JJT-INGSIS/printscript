package printscript.cli.internal.command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult
import printscript.cli.internal.ExitCode
import printscript.cli.internal.OperationOutcome
import printscript.cli.internal.report.ErrorReporter
import printscript.interpreter.InterpretationResult
import printscript.interpreter.Interpreter
import printscript.source.SourceReader
import printscript.source.SourceReaderCreationResult
import printscript.source.SourceReaderFactory
import printscript.statement.StatementSource
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

    /*
     * Clikt señaliza el código de salida lanzando ProgramResult. Es su
     * protocolo, no manejo de errores: el resto del CLI sigue devolviendo
     * resultados. Salir sin lanzar ya equivale al código 0.
     */
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
