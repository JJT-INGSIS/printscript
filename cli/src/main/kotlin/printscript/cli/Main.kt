package printscript.cli

import printscript.cli.internal.CliApplication
import printscript.cli.internal.SourceOperationRunner
import printscript.cli.internal.arguments.CliArgumentsParser
import printscript.cli.internal.io.ConsoleTerminal
import printscript.cli.internal.operation.AnalysisOperation
import printscript.cli.internal.operation.ExecutionOperation
import printscript.cli.internal.operation.FormattingOperation
import printscript.cli.internal.operation.SourceOperationRegistry
import printscript.cli.internal.operation.ValidationOperation
import printscript.cli.internal.pipeline.StatementSourcePipeline
import printscript.cli.internal.report.DiagnosticReporter
import printscript.cli.internal.report.ErrorReporter
import kotlin.system.exitProcess

public fun main(args: Array<String>) {
    val terminal = ConsoleTerminal()
    val errorReporter = ErrorReporter()
    val diagnosticReporter = DiagnosticReporter()

    val application = CliApplication(
        terminal = terminal,
        argumentsParser = CliArgumentsParser(),
        operations = SourceOperationRegistry(
            operationsByName = mapOf(
                "validation" to ValidationOperation(errorReporter),
                "execution" to ExecutionOperation(errorReporter),
                "formatting" to FormattingOperation(errorReporter),
                "analyzing" to AnalysisOperation(errorReporter, diagnosticReporter),
            ),
        ),
        runner = SourceOperationRunner(
            terminal = terminal,
            pipeline = StatementSourcePipeline(),
            errorReporter = errorReporter,
        ),
    )

    exitProcess(
        application.runCommandLine(args.toList()).value,
    )
}
