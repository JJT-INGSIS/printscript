package printscript.cli.internal.arguments

import printscript.cli.internal.operation.SourceOperationRequest

internal sealed interface ArgumentsParsingResult {

    data class Success(
        val operationName: String,
        val request: SourceOperationRequest,
    ) : ArgumentsParsingResult

    data class Failure(
        val message: String,
    ) : ArgumentsParsingResult
}
