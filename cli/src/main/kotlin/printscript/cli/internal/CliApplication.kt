package printscript.cli.internal

import printscript.cli.internal.arguments.ArgumentsParsingResult
import printscript.cli.internal.arguments.CliArgumentsParser
import printscript.cli.internal.io.Terminal
import printscript.cli.internal.operation.SourceOperationRegistry

/**
 * Adaptador de línea de comandos: traduce `argv` a una operación y una
 * petición, y delega el trabajo real en [SourceOperationRunner].
 *
 * Todo lo que queda en esta clase es exactamente lo que Clikt reemplaza
 * en la Fase 3. Cuando eso pase, la clase desaparece entera.
 */
internal class CliApplication(
    private val terminal: Terminal,
    private val argumentsParser: CliArgumentsParser,
    private val operations: SourceOperationRegistry,
    private val runner: SourceOperationRunner,
) {

    fun runCommandLine(commandLineArguments: List<String>): ExitCode {
        val parsing = when (
            val result = argumentsParser.parseArguments(commandLineArguments)
        ) {
            is ArgumentsParsingResult.Failure -> return reportUsageError(result.message)
            is ArgumentsParsingResult.Success -> result
        }

        val operation = operations.operationNamed(parsing.operationName)
            ?: return reportUsageError(
                "La operación '${parsing.operationName}' no existe. " +
                    "Disponibles: ${operations.availableOperationNames().joinToString()}",
            )

        return runner.exitCodeFor(
            operation = operation,
            request = parsing.request,
        )
    }

    private fun reportUsageError(message: String): ExitCode {
        terminal.writeErrorLine(message)

        return ExitCode.USAGE_ERROR
    }
}
