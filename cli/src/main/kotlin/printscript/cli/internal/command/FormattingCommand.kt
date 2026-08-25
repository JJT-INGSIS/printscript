package printscript.cli.internal.command

import printscript.cli.internal.arguments.CliArguments
import printscript.cli.internal.io.Terminal
import printscript.cli.internal.report.ErrorReporter
import printscript.formatter.FormattedSource
import printscript.formatter.FormattedStatementReadResult
import printscript.formatter.Formatter
import printscript.formatter.FormatterConfiguration
import printscript.formatter.PrintScriptFormatterFactory
import printscript.statement.StatementSource

internal class FormattingCommand(
    private val errorReporter: ErrorReporter,
    private val configuration: FormatterConfiguration =
        PrintScriptFormatterFactory.defaultV1Configuration(),
    private val createFormatter: (FormatterConfiguration) -> Formatter =
        PrintScriptFormatterFactory::createV1,
) : CliCommand {

    override val operationName: String = "formatting"

    override fun runOperation(
        arguments: CliArguments,
        statements: StatementSource,
        terminal: Terminal,
    ): CommandOutcome {
        return writeRemainingFormattedStatements(
            source = createFormatter(configuration).format(statements),
            terminal = terminal,
        )
    }

    private tailrec fun writeRemainingFormattedStatements(
        source: FormattedSource,
        terminal: Terminal,
    ): CommandOutcome {
        return when (val readResult = source.nextFormattedStatement()) {
            FormattedStatementReadResult.EndOfInput -> CommandOutcome.Success

            is FormattedStatementReadResult.Failure ->
                CommandOutcome.Failure(
                    errorReporter.describe(readResult.error),
                )

            is FormattedStatementReadResult.Success -> {
                terminal.writePreformatted(readResult.formattedText)

                writeRemainingFormattedStatements(readResult.remainingSource, terminal)
            }
        }
    }
}
