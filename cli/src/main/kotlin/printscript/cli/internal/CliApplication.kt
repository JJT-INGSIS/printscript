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
import printscript.cli.internal.report.ErrorReporter
import printscript.source.SourceReader
import printscript.source.SourceReaderCreationResult
import printscript.source.SourceReaderFactory
import printscript.statement.StatementSource
import java.nio.file.Files
import java.nio.file.Path

internal class CliApplication(
    private val terminal: Terminal,
    private val argumentsParser: CliArgumentsParser,
    private val pipeline: StatementSourcePipeline,
    private val commandDispatcher: CommandDispatcher,
    private val errorReporter: ErrorReporter,
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

        val sourceReader = when (
            val creation = SourceReaderFactory.fromPath(arguments.sourceFilePath)
        ) {
            is SourceReaderCreationResult.Failure ->
                return reportSourceError(errorReporter.describe(creation.error))

            is SourceReaderCreationResult.Success -> creation.reader
        }

        return runCommand(
            command = command,
            arguments = arguments,
            sourceReader = sourceReader,
        )
    }

    private fun runCommand(command: CliCommand, arguments: CliArguments, sourceReader: SourceReader): ExitCode {
        val outcome = command.runOperation(
            arguments = arguments,
            statements = progressReportingStatementsOf(sourceReader, arguments),
            terminal = terminal,
        )

        return when (outcome) {
            CommandOutcome.Success -> ExitCode.SUCCESS
            is CommandOutcome.Failure -> reportSourceError(outcome.message)
        }
    }

    /**
     * Arma el pipeline y lo envuelve con el reporte de progreso.
     *
     * El progreso entra por un decorator y no modificando a los
     * consumidores: ni el intérprete ni el formatter ni el analizador
     * tienen un solo parámetro dedicado a esto.
     */
    private fun progressReportingStatementsOf(sourceReader: SourceReader, arguments: CliArguments): StatementSource {
        val statements = pipeline.statementsFrom(
            sourceReader = sourceReader,
            version = arguments.version,
        )

        return ProgressReportingStatementSource(
            delegate = statements,
            totalBytes = sourceSizeOf(arguments.sourceFilePath),
            onProgress = { percentage -> terminal.writeError("parsing... $percentage%") },
        )
    }

    private fun sourceSizeOf(sourceFilePath: Path): Long {
        return runCatching { Files.size(sourceFilePath) }.getOrDefault(0L)
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
