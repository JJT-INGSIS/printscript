package printscript.cli.internal

import printscript.cli.internal.io.Terminal
import printscript.cli.internal.operation.OperationOutcome
import printscript.cli.internal.operation.SourceOperation
import printscript.cli.internal.operation.SourceOperationRequest
import printscript.cli.internal.pipeline.StatementSourcePipeline
import printscript.cli.internal.progress.ProgressReportingStatementSource
import printscript.cli.internal.report.ErrorReporter
import printscript.source.SourceReader
import printscript.source.SourceReaderCreationResult
import printscript.source.SourceReaderFactory
import printscript.statement.StatementSource
import java.nio.file.Files
import java.nio.file.Path

internal class SourceOperationRunner(
    private val terminal: Terminal,
    private val pipeline: StatementSourcePipeline,
    private val errorReporter: ErrorReporter,
) {

    fun exitCodeFor(operation: SourceOperation, request: SourceOperationRequest): ExitCode {
        val sourceReader = when (
            val creation = SourceReaderFactory.fromPath(request.sourceFilePath)
        ) {
            is SourceReaderCreationResult.Failure ->
                return reportSourceError(errorReporter.describe(creation.error))

            is SourceReaderCreationResult.Success -> creation.reader
        }

        return exitCodeFor(
            outcome = operation.outcomeFor(
                statements = progressReportingStatementsOf(sourceReader, request),
                terminal = terminal,
            ),
        )
    }

    private fun exitCodeFor(outcome: OperationOutcome): ExitCode {
        return when (outcome) {
            OperationOutcome.Success -> ExitCode.SUCCESS
            is OperationOutcome.CompletedWithFindings -> reportFindings(outcome.message)
            is OperationOutcome.Failure -> reportSourceError(outcome.message)
        }
    }

    private fun progressReportingStatementsOf(
        sourceReader: SourceReader,
        request: SourceOperationRequest,
    ): StatementSource {
        val statements = pipeline.statementsFrom(
            sourceReader = sourceReader,
            version = request.version,
        )

        return ProgressReportingStatementSource(
            delegate = statements,
            totalBytes = sourceSizeOf(request.sourceFilePath),
            onProgress = { percentage -> terminal.writeErrorLine("parsing... $percentage%") },
        )
    }

    private fun sourceSizeOf(sourceFilePath: Path): Long {
        return runCatching { Files.size(sourceFilePath) }.getOrDefault(0L)
    }

    private fun reportFindings(message: String): ExitCode {
        terminal.writeLine(message)

        return ExitCode.FINDINGS
    }

    private fun reportSourceError(message: String): ExitCode {
        terminal.writeErrorLine(message)

        return ExitCode.SOURCE_ERROR
    }
}
