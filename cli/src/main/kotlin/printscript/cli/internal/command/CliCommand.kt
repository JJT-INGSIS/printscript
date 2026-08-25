package printscript.cli.internal.command

import printscript.cli.internal.arguments.CliArguments
import printscript.cli.internal.io.Terminal
import printscript.statement.StatementSource

internal interface CliCommand {

    val operationName: String

    fun runOperation(arguments: CliArguments, statements: StatementSource, terminal: Terminal): CommandOutcome
}
