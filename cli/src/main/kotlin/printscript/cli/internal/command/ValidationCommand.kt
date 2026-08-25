package printscript.cli.internal.command

import printscript.cli.internal.arguments.CliArguments
import printscript.cli.internal.io.Terminal
import printscript.cli.internal.report.ErrorReporter
import printscript.statement.StatementReadResult
import printscript.statement.StatementSource

/**
 * Recorre el programa entero sin ejecutarlo, para reportar únicamente
 * errores de sintaxis.
 *
 * No usa ningún consumidor: el trabajo lo hace el parser, y este
 * comando solo tira de la fuente hasta el final.
 */
internal class ValidationCommand(
    private val errorReporter: ErrorReporter,
) : CliCommand {

    override val operationName: String = "validation"

    override fun runOperation(
        arguments: CliArguments,
        statements: StatementSource,
        terminal: Terminal,
    ): CommandOutcome {
        val outcome = validateRemaining(statements)

        if (outcome is CommandOutcome.Success) {
            terminal.writeLine("El archivo no tiene errores de sintaxis.")
        }

        return outcome
    }

    /**
     * `tailrec` para no necesitar una variable mutable ni arriesgar un
     * desborde de pila: el compilador lo convierte en un bucle.
     */
    private tailrec fun validateRemaining(source: StatementSource): CommandOutcome {
        return when (val readResult = source.nextStatement()) {
            StatementReadResult.EndOfInput ->
                CommandOutcome.Success

            is StatementReadResult.Failure ->
                CommandOutcome.Failure(
                    errorReporter.describe(readResult.error),
                )

            is StatementReadResult.Success ->
                validateRemaining(readResult.remainingSource)
        }
    }
}
