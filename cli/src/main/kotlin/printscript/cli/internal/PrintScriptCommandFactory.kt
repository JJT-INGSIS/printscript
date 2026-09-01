package printscript.cli.internal

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import printscript.cli.internal.command.AnalysisCommand
import printscript.cli.internal.command.ExecutionCommand
import printscript.cli.internal.command.FormattingCommand
import printscript.cli.internal.command.PrintScriptCommandGroup
import printscript.cli.internal.command.ValidationCommand
import printscript.cli.internal.operation.AnalysisOperation
import printscript.cli.internal.operation.ExecutionOperation
import printscript.cli.internal.operation.FormattingOperation
import printscript.cli.internal.operation.LanguageVersion
import printscript.cli.internal.operation.SourceOperationFactory
import printscript.cli.internal.operation.ValidationOperation
import printscript.cli.internal.report.DiagnosticReporter
import printscript.cli.internal.report.ErrorReporter

internal object PrintScriptCommandFactory {

    fun create(): CliktCommand {
        val errorReporter = ErrorReporter()
        val diagnosticReporter = DiagnosticReporter()

        return PrintScriptCommandGroup().subcommands(
            ValidationCommand(
                operationFactory = SourceOperationFactory { request ->
                    when (request.version) {
                        LanguageVersion.V1_0 -> ValidationOperation(errorReporter)
                    }
                },
                errorReporter = errorReporter,
            ),
            ExecutionCommand(
                operationFactory = SourceOperationFactory { request ->
                    when (request.version) {
                        LanguageVersion.V1_0 -> ExecutionOperation(errorReporter)
                    }
                },
                errorReporter = errorReporter,
            ),
            FormattingCommand(
                operationFactory = SourceOperationFactory { request ->
                    when (request.version) {
                        LanguageVersion.V1_0 -> FormattingOperation(errorReporter)
                    }
                },
                errorReporter = errorReporter,
            ),
            AnalysisCommand(
                operationFactory = SourceOperationFactory { request ->
                    when (request.version) {
                        LanguageVersion.V1_0 -> AnalysisOperation(
                            errorReporter = errorReporter,
                            diagnosticReporter = diagnosticReporter,
                        )
                    }
                },
                errorReporter = errorReporter,
            ),
        )
    }
}
