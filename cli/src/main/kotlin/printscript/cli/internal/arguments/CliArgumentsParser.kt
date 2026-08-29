package printscript.cli.internal.arguments

import printscript.cli.internal.operation.LanguageVersion
import printscript.cli.internal.operation.SourceOperationRequest
import java.nio.file.Path

private const val OPERATION_INDEX = 0
private const val SOURCE_FILE_INDEX = 1
private const val REQUIRED_ARGUMENT_COUNT = 2
private const val MAXIMUM_OPTIONAL_ARGUMENT_COUNT = 2

internal class CliArgumentsParser {

    fun parseArguments(commandLineArguments: List<String>): ArgumentsParsingResult {
        if (commandLineArguments.size < REQUIRED_ARGUMENT_COUNT) {
            return ArgumentsParsingResult.Failure(
                "Faltan argumentos. Se esperaba: <operación> <archivo> [versión] [configuración]",
            )
        }

        val rawSourceFilePath = commandLineArguments[SOURCE_FILE_INDEX]

        val sourceFilePath = pathOf(rawSourceFilePath)
            ?: return ArgumentsParsingResult.Failure(
                "La ruta '$rawSourceFilePath' no es válida.",
            )

        return parseOptionalArguments(
            operationName = commandLineArguments[OPERATION_INDEX].lowercase(),
            sourceFilePath = sourceFilePath,
            optionalArguments = commandLineArguments.drop(REQUIRED_ARGUMENT_COUNT),
        )
    }

    private fun parseOptionalArguments(
        operationName: String,
        sourceFilePath: Path,
        optionalArguments: List<String>,
    ): ArgumentsParsingResult {
        if (optionalArguments.size > MAXIMUM_OPTIONAL_ARGUMENT_COUNT) {
            return ArgumentsParsingResult.Failure(
                "Demasiados argumentos: se recibieron ${optionalArguments.size} opcionales.",
            )
        }

        val versions = optionalArguments.mapNotNull(LanguageVersion::fromLabel)

        if (versions.size > 1) {
            return ArgumentsParsingResult.Failure(
                "Se especificó la versión más de una vez.",
            )
        }

        val configurationFilePaths = optionalArguments.filter { argument ->
            LanguageVersion.fromLabel(argument) == null
        }

        if (configurationFilePaths.size > 1) {
            return ArgumentsParsingResult.Failure(
                "Se especificó el archivo de configuración más de una vez.",
            )
        }

        return ArgumentsParsingResult.Success(
            operationName = operationName,
            request = SourceOperationRequest(
                sourceFilePath = sourceFilePath,
                version = versions.firstOrNull() ?: LanguageVersion.DEFAULT,
                configurationFilePath = configurationFilePaths.firstOrNull(),
            ),
        )
    }

    private fun pathOf(rawPath: String): Path? {
        return runCatching { Path.of(rawPath) }.getOrNull()
    }
}
