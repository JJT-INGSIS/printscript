package printscript.cli

import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import printscript.cli.internal.command.AnalysisCommand
import printscript.cli.internal.command.ExecutionCommand
import printscript.cli.internal.command.FormattingCommand
import printscript.cli.internal.command.PrintScriptCommandGroup
import printscript.cli.internal.command.ValidationCommand
import printscript.cli.internal.report.DiagnosticReporter
import printscript.cli.internal.report.ErrorReporter

public fun main(args: Array<String>) {
    val errorReporter = ErrorReporter()
    val diagnosticReporter = DiagnosticReporter()

    PrintScriptCommandGroup()
        .subcommands(
            ValidationCommand(errorReporter),
            ExecutionCommand(errorReporter),
            FormattingCommand(errorReporter),
            AnalysisCommand(errorReporter, diagnosticReporter),
        )
        .main(args)
}
