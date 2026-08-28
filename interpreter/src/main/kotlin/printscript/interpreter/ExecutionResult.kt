package printscript.interpreter

/** Result of an interpreter strategy operation. */
public sealed interface ExecutionResult<out T> {

    public data class Success<T>(
        public val value: T,
    ) : ExecutionResult<T>

    public data class Failure(
        public val error: SemanticError,
    ) : ExecutionResult<Nothing>
}
