package printscript.cli

import printscript.cli.internal.CliApplication
import printscript.cli.internal.arguments.CliArgumentsParser
import printscript.cli.internal.command.CommandDispatcher
import printscript.cli.internal.command.ExecutionCommand
import printscript.cli.internal.command.ValidationCommand
import printscript.cli.internal.io.ConsoleTerminal
import printscript.cli.internal.pipeline.StatementSourcePipeline
import printscript.cli.internal.report.ErrorReporter
import kotlin.system.exitProcess

public fun main(args: Array<String>) {
    val errorReporter = ErrorReporter()
    val application = CliApplication(
        terminal = ConsoleTerminal(),
        argumentsParser = CliArgumentsParser(),
        pipeline = StatementSourcePipeline(),
        errorReporter = errorReporter,
        commandDispatcher = CommandDispatcher(
            commands = listOf(
                ValidationCommand(errorReporter),
                ExecutionCommand(errorReporter),
            ),
        ),
    )

    exitProcess(
        application.runCommandLine(args.toList()).value,
    )
}
