package printscript.cli.internal.command

import printscript.cli.internal.arguments.CliArguments
import printscript.cli.internal.io.Terminal
import printscript.cli.internal.report.DiagnosticReporter
import printscript.cli.internal.report.ErrorReporter
import printscript.linter.DiagnosticReadResult
import printscript.linter.DiagnosticSource
import printscript.linter.Linter
import printscript.linter.LinterConfiguration
import printscript.linter.PrintScriptLinterFactory
import printscript.statement.StatementSource

internal class AnalysisCommand(
    private val errorReporter: ErrorReporter,
    private val diagnosticReporter: DiagnosticReporter,
    private val configuration: LinterConfiguration =
        PrintScriptLinterFactory.defaultV1Configuration(),
    private val createLinter: (LinterConfiguration) -> Linter =
        PrintScriptLinterFactory::createV1,
) : CliCommand {

    override val operationName: String = "analyzing"

    override fun runOperation(
        arguments: CliArguments,
        statements: StatementSource,
        terminal: Terminal,
    ): CommandOutcome {
        return reportRemainingDiagnostics(
            source = createLinter(configuration).lint(statements),
            terminal = terminal,
            reportedCount = 0,
        )
    }

    /**
     * `reportedCount` viaja como parámetro y no como campo, así la clase
     * no guarda estado entre corridas.
     */
    private tailrec fun reportRemainingDiagnostics(
        source: DiagnosticSource,
        terminal: Terminal,
        reportedCount: Int,
    ): CommandOutcome {
        return when (val readResult = source.nextDiagnostic()) {
            DiagnosticReadResult.EndOfInput -> reportSummaryOf(reportedCount, terminal)

            is DiagnosticReadResult.Failure ->
                CommandOutcome.Failure(
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

    private fun reportSummaryOf(reportedCount: Int, terminal: Terminal): CommandOutcome {
        if (reportedCount == 0) {
            terminal.writeLine("No se encontraron problemas.")

            return CommandOutcome.Success
        }

        return CommandOutcome.CompletedWithFindings(
            "Se encontraron $reportedCount problemas.",
        )
    }
}
