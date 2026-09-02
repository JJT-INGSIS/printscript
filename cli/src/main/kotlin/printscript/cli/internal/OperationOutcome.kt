package printscript.cli.internal

internal sealed interface OperationOutcome {

    data object Success : OperationOutcome

    data class CompletedWithFindings(
        val message: String,
    ) : OperationOutcome

    data class Failure(
        val message: String,
    ) : OperationOutcome
}
