package printscript.v1.interpreter.internal

import printscript.interpreter.ExecutionResult

internal inline fun <S> ExecutionResult<S>.orReturn(onFailure: (ExecutionResult.Failure) -> Nothing): S = when (this) {
    is ExecutionResult.Success -> value
    is ExecutionResult.Failure -> onFailure(this)
}
