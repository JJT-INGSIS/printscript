package printscript.cli.internal.operation

import printscript.cli.internal.io.Terminal
import printscript.cli.internal.report.DiagnosticReporter
import printscript.cli.internal.report.ErrorReporter
import printscript.linter.DiagnosticReadResult
import printscript.linter.DiagnosticSource
import printscript.linter.Linter
import printscript.statement.StatementSource
import printscript.v1.linter.PrintScriptV1LinterConfiguration
import printscript.v1.linter.PrintScriptV1LinterFactory

internal class AnalysisOperation(
    private val errorReporter: ErrorReporter,
    private val diagnosticReporter: DiagnosticReporter,
    private val configuration: PrintScriptV1LinterConfiguration =
        PrintScriptV1LinterFactory.defaultConfiguration(),
    private val createLinter: (PrintScriptV1LinterConfiguration) -> Linter =
        { linterConfiguration ->
            PrintScriptV1LinterFactory.create(
                configuration = linterConfiguration,
            )
        },
) : SourceOperation {

    override fun outcomeFor(statements: StatementSource, terminal: Terminal): OperationOutcome {
        return reportRemainingDiagnostics(
            source = createLinter(configuration).lint(statements),
            terminal = terminal,
            reportedCount = 0,
        )
    }

    private tailrec fun reportRemainingDiagnostics(
        source: DiagnosticSource,
        terminal: Terminal,
        reportedCount: Int,
    ): OperationOutcome {
        return when (val readResult = source.nextDiagnostic()) {
            DiagnosticReadResult.EndOfInput -> reportSummaryOf(reportedCount, terminal)

            is DiagnosticReadResult.Failure ->
                OperationOutcome.Failure(
                    errorReporter.describe(readResult.error),
                )

            is DiagnosticReadResult.Success -> {
                terminal.writeLine(diagnosticReporter.describe(readResult.diagnostic))

                reportRemainingDiagnostics(
                    source = readResult.remainingSource,
                    terminal = terminal,
                    reportedCount = reportedCount + 1,
                )
            }
        }
    }

    private fun reportSummaryOf(reportedCount: Int, terminal: Terminal): OperationOutcome {
        if (reportedCount == 0) {
            terminal.writeLine("No se encontraron problemas.")

            return OperationOutcome.Success
        }

        return OperationOutcome.CompletedWithFindings(
            "Se encontraron $reportedCount problemas.",
        )
    }
}
