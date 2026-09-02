package printscript.cli.internal

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import printscript.cli.internal.command.AnalysisCommand
import printscript.cli.internal.command.ExecutionCommand
import printscript.cli.internal.command.FormattingCommand
import printscript.cli.internal.command.PrintScriptCommandGroup
import printscript.cli.internal.command.ValidationCommand
import printscript.cli.internal.report.DiagnosticReporter
import printscript.cli.internal.report.ErrorReporter

internal object PrintScriptCommandFactory {

    fun create(): CliktCommand {
        val errorReporter = ErrorReporter()
        val diagnosticReporter = DiagnosticReporter()

        return PrintScriptCommandGroup().subcommands(
            ValidationCommand(errorReporter),
            ExecutionCommand(errorReporter),
            FormattingCommand(errorReporter),
            AnalysisCommand(errorReporter, diagnosticReporter),
        )
    }
}
