package printscript.cli.internal

import printscript.cli.internal.io.Terminal
import printscript.cli.internal.operation.OperationOutcome
import printscript.cli.internal.operation.SourceOperation
import printscript.cli.internal.operation.SourceOperationRequest
import printscript.cli.internal.pipeline.StatementSourcePipeline
import printscript.cli.internal.report.ErrorReporter
import printscript.source.SourceReader
import printscript.source.SourceReaderCreationResult
import printscript.source.SourceReaderFactory
import printscript.statement.StatementSource

internal class SourceOperationRunner(
    private val terminal: Terminal,
    private val pipeline: StatementSourcePipeline,
    private val errorReporter: ErrorReporter,
) {

    fun exitCodeFor(operation: SourceOperation, request: SourceOperationRequest): ExitCode {
        val outcome = outcomeOf(operation, request)

        reportOutcome(outcome)

        return exitCodeOf(outcome)
    }

    private fun outcomeOf(operation: SourceOperation, request: SourceOperationRequest): OperationOutcome {
        return when (
            val creation = SourceReaderFactory.fromPath(request.sourceFilePath)
        ) {
            is SourceReaderCreationResult.Failure ->
                OperationOutcome.Failure(
                    errorReporter.describe(creation.error),
                )

            is SourceReaderCreationResult.Success ->
                operation.outcomeFor(
                    statements = statementsOf(creation.reader, request),
                    terminal = terminal,
                )
        }
    }

    private fun reportOutcome(outcome: OperationOutcome) {
        when (outcome) {
            OperationOutcome.Success -> Unit

            is OperationOutcome.CompletedWithFindings -> terminal.writeLine(outcome.message)

            is OperationOutcome.Failure -> terminal.writeErrorLine(outcome.message)
        }
    }

    private fun exitCodeOf(outcome: OperationOutcome): ExitCode {
        return when (outcome) {
            OperationOutcome.Success -> ExitCode.SUCCESS

            is OperationOutcome.CompletedWithFindings -> ExitCode.FINDINGS

            is OperationOutcome.Failure -> ExitCode.SOURCE_ERROR
        }
    }

    private fun statementsOf(sourceReader: SourceReader, request: SourceOperationRequest): StatementSource {
        return pipeline.statementsFrom(
            sourceReader = sourceReader,
            version = request.version,
        )
    }
}
