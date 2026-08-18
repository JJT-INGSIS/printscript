package printscript.interpreter

inline fun <T> ExecutionResult<T>.orReturn( // cambiar clase para q se enteienda mas, esto no se enteiende nada
    onFailure: (ExecutionResult.Failure) -> Nothing,
): T =
    when (this) {
        is ExecutionResult.Success -> value
        is ExecutionResult.Failure -> onFailure(this)
    }

inline fun <T : Any> T?.orFail(
    error: () -> SemanticError,
): ExecutionResult<T> =
    if (this == null) {
        ExecutionResult.Failure(error())
    } else {
        ExecutionResult.Success(this)
    }