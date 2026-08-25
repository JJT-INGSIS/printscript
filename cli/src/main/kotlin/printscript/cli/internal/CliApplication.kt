package printscript.cli.internal

import printscript.cli.internal.arguments.ArgumentsParsingResult
import printscript.cli.internal.arguments.CliArguments
import printscript.cli.internal.arguments.CliArgumentsParser
import printscript.cli.internal.command.CliCommand
import printscript.cli.internal.command.CommandDispatcher
import printscript.cli.internal.command.CommandOutcome
import printscript.cli.internal.io.Terminal
import printscript.cli.internal.pipeline.StatementSourcePipeline
import printscript.cli.internal.progress.ProgressReportingStatementSource
import printscript.cli.internal.source.SourceCodeLoader
import printscript.cli.internal.source.SourceLoadingResult
import printscript.source.SourceReaderFactory
import printscript.statement.StatementSource

internal class CliApplication(
    private val terminal: Terminal,
    private val argumentsParser: CliArgumentsParser,
    private val sourceCodeLoader: SourceCodeLoader,
    private val pipeline: StatementSourcePipeline,
    private val commandDispatcher: CommandDispatcher,
) {
    fun runCommandLine(commandLineArguments: List<String>): ExitCode {
        val arguments = when (
            val parsing = argumentsParser.parseArguments(commandLineArguments)
        ) {
            is ArgumentsParsingResult.Failure -> return reportUsageError(parsing.message)
            is ArgumentsParsingResult.Success -> parsing.arguments
        }

        val command = commandDispatcher.commandFor(arguments.operationName)
            ?: return reportUsageError(
                "La operación '${arguments.operationName}' no existe. " +
                    "Disponibles: ${commandDispatcher.availableOperationNames().joinToString()}",
            )

        val sourceCode = when (
            val loading = sourceCodeLoader.loadSourceCode(arguments.sourceFilePath)
        ) {
            is SourceLoadingResult.Failure -> return reportSourceError(loading.message)
            is SourceLoadingResult.Success -> loading.sourceCode
        }

        return runCommand(
            command = command,
            arguments = arguments,
            sourceCode = sourceCode,
        )
    }

    private fun runCommand(command: CliCommand, arguments: CliArguments, sourceCode: String): ExitCode {
        val outcome = command.runOperation(
            arguments = arguments,
            statements = statementsOf(sourceCode, arguments),
            terminal = terminal,
        )

        return when (outcome) {
            CommandOutcome.Success -> ExitCode.SUCCESS
            is CommandOutcome.Failure -> reportSourceError(outcome.message)
        }
    }

    private fun statementsOf(sourceCode: String, arguments: CliArguments): StatementSource {
        val statements = pipeline.statementsFrom(
            sourceReader = SourceReaderFactory.fromString(sourceCode),
            version = arguments.version,
        )

        return ProgressReportingStatementSource(
            delegate = statements,
            totalCharacters = sourceCode.length.toLong(),
            onProgress = { percentage -> terminal.writeError("parsing... $percentage%") },
        )
    }

    private fun reportUsageError(message: String): ExitCode {
        terminal.writeError(message)

        return ExitCode.USAGE_ERROR
    }

    private fun reportSourceError(message: String): ExitCode {
        terminal.writeError(message)

        return ExitCode.SOURCE_ERROR
    }
}
