package printscript.cli.internal.command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult
import printscript.cli.internal.ExitCode
import printscript.cli.internal.OperationOutcome
import printscript.cli.internal.report.ErrorReporter
import printscript.source.SourceReader
import printscript.source.SourceReaderCreationResult
import printscript.source.SourceReaderFactory
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
