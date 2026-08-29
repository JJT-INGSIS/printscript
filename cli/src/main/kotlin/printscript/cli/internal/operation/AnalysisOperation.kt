package printscript.cli.internal.operation

import printscript.cli.internal.io.Terminal
import printscript.cli.internal.report.DiagnosticReporter
import printscript.cli.internal.report.ErrorReporter
import printscript.linter.DiagnosticReadResult
import printscript.linter.DiagnosticSource
import printscript.linter.Linter
import printscript.linter.LinterConfiguration
import printscript.linter.PrintScriptLinterFactory
import printscript.statement.StatementSource

internal class AnalysisOperation(
    private val errorReporter: ErrorReporter,
    private val diagnosticReporter: DiagnosticReporter,
    private val configuration: LinterConfiguration =
        PrintScriptLinterFactory.defaultV1Configuration(),
    private val createLinter: (LinterConfiguration) -> Linter =
        PrintScriptLinterFactory::createV1,
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
