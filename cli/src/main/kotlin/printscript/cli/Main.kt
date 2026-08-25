package printscript.cli

import printscript.cli.internal.CliApplication
import printscript.cli.internal.arguments.CliArgumentsParser
import printscript.cli.internal.command.CommandDispatcher
import printscript.cli.internal.command.ExecutionCommand
import printscript.cli.internal.command.ValidationCommand
import printscript.cli.internal.io.ConsoleTerminal
import printscript.cli.internal.pipeline.StatementSourcePipeline
import printscript.cli.internal.report.ErrorReporter
import printscript.cli.internal.source.SourceCodeLoader
import kotlin.system.exitProcess

public fun main(args: Array<String>) {
    val application = CliApplication(
        terminal = ConsoleTerminal(),
        argumentsParser = CliArgumentsParser(),
        sourceCodeLoader = SourceCodeLoader(),
        pipeline = StatementSourcePipeline(),
        errorReporter = ErrorReporter(),
        commandDispatcher = CommandDispatcher(
            commands = listOf(
                ValidationCommand(ErrorReporter()),
                ExecutionCommand(ErrorReporter()),
            ),
        ),
    )

    exitProcess(
        application.run(args.toList()).value,
    )
}
