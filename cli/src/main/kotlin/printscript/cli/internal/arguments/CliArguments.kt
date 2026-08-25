package printscript.cli.internal.arguments

internal data class CliArguments(
    val operationName: String,
    val sourceFilePath: String,
    val version: LanguageVersion,
    val configurationFilePath: String?,
)
