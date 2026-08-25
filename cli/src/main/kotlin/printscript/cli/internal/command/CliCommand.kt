package printscript.cli.internal.command

import printscript.cli.internal.arguments.CliArguments
import printscript.cli.internal.io.Terminal
import printscript.statement.StatementSource

/**
 * Una operación del CLI.
 *
 * Recibe el [StatementSource] ya armado: ningún comando sabe que
 * existen un archivo, un lexer o un parser. Eso lo resuelve
 * [printscript.cli.internal.CliApplication] una sola vez para los
 * cuatro, y es lo que hace que agregar una operación sea escribir una
 * clase y sumarla a una lista.
 *
 * [arguments] llega porque las operaciones de formateo y análisis van a
 * necesitar la ruta del archivo de configuración.
 */
internal interface CliCommand {

    val operationName: String

    fun runOperation(arguments: CliArguments, statements: StatementSource, terminal: Terminal): CommandOutcome
}
