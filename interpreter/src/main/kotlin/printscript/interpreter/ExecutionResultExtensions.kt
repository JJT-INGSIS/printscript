package printscript.interpreter

inline fun <T> ExecutionResult<T>.orReturn(
    onFailure: (ExecutionResult.Failure) -> Nothing,
): T =
    when (this) {
        is ExecutionResult.Success -> value
        is ExecutionResult.Failure -> onFailure(this)
    }