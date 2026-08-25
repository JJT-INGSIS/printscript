package printscript.cli.internal.source

internal sealed interface SourceLoadingResult {

    data class Success(
        val sourceCode: String,
    ) : SourceLoadingResult

    data class Failure(
        val message: String,
    ) : SourceLoadingResult
}
