package printscript.cli.internal.command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult
import printscript.cli.internal.ExitCode
import printscript.cli.internal.OperationOutcome
import printscript.cli.internal.report.ErrorReporter
import printscript.source.SourceReader
import printscript.source.SourceReaderCreationResult
import printscript.source.SourceReaderFactory
import java.io.IOException
import java.nio.file.AccessDeniedException
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path

internal fun CliktCommand.runOnSourceFile(
    sourceFilePath: Path,
    errorReporter: ErrorReporter,
    outcomeFrom: (SourceReader) -> OperationOutcome,
) {
    val outcome = operationOutcomeFromSourceFile(
        sourceFilePath = sourceFilePath,
        errorReporter = errorReporter,
        outcomeFrom = outcomeFrom,
    )

    reportOutcome(outcome)

    val exitCode = exitCodeOf(outcome)

    if (exitCode != ExitCode.SUCCESS) {
        throw ProgramResult(exitCode.value)
    }
}

private fun operationOutcomeFromSourceFile(
    sourceFilePath: Path,
    errorReporter: ErrorReporter,
    outcomeFrom: (SourceReader) -> OperationOutcome,
): OperationOutcome {
    if (!Files.exists(sourceFilePath)) {
        return OperationOutcome.Failure(errorReporter.describeMissingSourceFile(sourceFilePath))
    }

    if (!Files.isRegularFile(sourceFilePath)) {
        return OperationOutcome.Failure(errorReporter.describeInvalidSourceFile(sourceFilePath))
    }

    if (!Files.isReadable(sourceFilePath)) {
        return OperationOutcome.Failure(errorReporter.describeUnreadableSourceFile(sourceFilePath))
    }

    return try {
        Files.newInputStream(sourceFilePath).use { inputStream ->
            when (val creation = SourceReaderFactory.fromInputStream(inputStream)) {
                is SourceReaderCreationResult.Failure ->
                    OperationOutcome.Failure(errorReporter.describe(creation.error))

                is SourceReaderCreationResult.Success ->
                    outcomeFrom(creation.reader)
            }
        }
    } catch (_: NoSuchFileException) {
        OperationOutcome.Failure(errorReporter.describeMissingSourceFile(sourceFilePath))
    } catch (_: AccessDeniedException) {
        OperationOutcome.Failure(errorReporter.describeUnreadableSourceFile(sourceFilePath))
    } catch (cause: IOException) {
        OperationOutcome.Failure(
            errorReporter.describeSourceFileAccessFailure(
                path = sourceFilePath,
                reason = cause.message.orEmpty(),
            ),
        )
    } catch (_: SecurityException) {
        OperationOutcome.Failure(errorReporter.describeUnreadableSourceFile(sourceFilePath))
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
