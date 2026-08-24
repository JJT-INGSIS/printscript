package printscript.cli.internal.arguments

internal sealed interface ArgumentsParsingResult {

    data class Success(
        val arguments: CliArguments,
    ) : ArgumentsParsingResult

    data class Failure(
        val message: String,
    ) : ArgumentsParsingResult
}