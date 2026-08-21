package printscript.interpreter

internal sealed interface ExecutionResult<out T> {

    data class Success<T>(
        val value: T,
    ) : ExecutionResult<T>

    data class Failure(
        val error: SemanticError,
    ) : ExecutionResult<Nothing>
}
