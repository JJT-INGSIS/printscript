package printscript.cli.internal.arguments

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

        val optionalArguments = commandLineArguments.drop(REQUIRED_ARGUMENT_COUNT)

        if (optionalArguments.size > MAXIMUM_OPTIONAL_ARGUMENT_COUNT) {
            return ArgumentsParsingResult.Failure(
                "Demasiados argumentos: se recibieron ${commandLineArguments.size}.",
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
            CliArguments(
                operationName = commandLineArguments[OPERATION_INDEX].lowercase(),
                sourceFilePath = commandLineArguments[SOURCE_FILE_INDEX],
                version = versions.firstOrNull() ?: LanguageVersion.DEFAULT,
                configurationFilePath = configurationFilePaths.firstOrNull(),
            ),
        )
    }
}
