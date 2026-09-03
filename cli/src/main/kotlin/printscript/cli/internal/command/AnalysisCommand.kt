package printscript.cli.internal.command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import printscript.cli.internal.OperationOutcome
import printscript.cli.internal.report.DiagnosticReporter
import printscript.cli.internal.report.ErrorReporter
import printscript.cli.internal.toolchain.LanguageVersion
import printscript.cli.internal.toolchain.PrintScriptToolchain
import printscript.cli.internal.toolchain.PrintScriptToolchainFactory
import printscript.linter.DiagnosticReadResult
import printscript.linter.DiagnosticSource

internal class AnalysisCommand(
    private val errorReporter: ErrorReporter,
    private val diagnosticReporter: DiagnosticReporter,
    private val toolchainFor: (LanguageVersion) -> PrintScriptToolchain =
        PrintScriptToolchainFactory::forVersion,
) : CliktCommand(name = "analysis") {

    private val sourceFilePath by sourceFileArgument()

    private val languageOptions by LanguageOptions()

    override fun help(context: Context): String {
        return "Reporta problemas de estilo sin modificar el archivo"
    }

    override fun run() {
        val toolchain = toolchainFor(languageOptions.version)

        runOnSourceFile(
            sourceFilePath = sourceFilePath,
            errorReporter = errorReporter,
        ) { sourceReader ->
            reportRemainingDiagnostics(
                source = toolchain.linter().lint(
                    toolchain.statementsFrom(sourceReader),
                ),
                reportedCount = 0,
            )
        }
    }

    private tailrec fun reportRemainingDiagnostics(source: DiagnosticSource, reportedCount: Int): OperationOutcome {
        return when (val readResult = source.nextDiagnostic()) {
            DiagnosticReadResult.EndOfInput -> summaryOf(reportedCount)

            is DiagnosticReadResult.Failure ->
                OperationOutcome.Failure(
                    errorReporter.describe(readResult.error),
                )

            is DiagnosticReadResult.Success -> {
                echo(diagnosticReporter.describe(readResult.diagnostic))

                reportRemainingDiagnostics(
                    source = readResult.remainingSource,
                    reportedCount = reportedCount + 1,
                )
            }
        }
    }

    private fun summaryOf(reportedCount: Int): OperationOutcome {
        if (reportedCount == 0) {
            echo("No se encontraron problemas.")

            return OperationOutcome.Success
        }

        return OperationOutcome.CompletedWithFindings(
            findingsMessageFor(reportedCount),
        )
    }

    private fun findingsMessageFor(reportedCount: Int): String {
        return if (reportedCount == 1) {
            "Se encontró 1 problema."
        } else {
            "Se encontraron $reportedCount problemas."
        }
    }
}
