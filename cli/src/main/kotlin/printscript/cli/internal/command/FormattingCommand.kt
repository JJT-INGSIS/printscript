package printscript.cli.internal.command

import printscript.cli.internal.arguments.CliArguments
import printscript.cli.internal.io.Terminal
import printscript.cli.internal.report.ErrorReporter
import printscript.formatter.FormattedSource
import printscript.formatter.FormattedStatementReadResult
import printscript.formatter.Formatter
import printscript.statement.StatementSource
import printscript.v1.formatter.PrintScriptV1FormatterConfiguration
import printscript.v1.formatter.PrintScriptV1FormatterFactory

internal class FormattingCommand(
    private val errorReporter: ErrorReporter,
    private val configuration: PrintScriptV1FormatterConfiguration =
        PrintScriptV1FormatterFactory.defaultConfiguration(),
    private val createFormatter: (PrintScriptV1FormatterConfiguration) -> Formatter =
        { formatterConfiguration ->
            PrintScriptV1FormatterFactory.create(
                configuration = formatterConfiguration,
            )
        },
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
