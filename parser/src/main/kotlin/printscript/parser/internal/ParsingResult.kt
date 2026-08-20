package printscript.parser.internal

import printscript.parser.internal.context.ParsingContext
import printscript.statement.ParseError

internal sealed interface ParsingResult<out T> {
    data class Success<out T>(
        val value: T,
        val resultingContext: ParsingContext,
    ) : ParsingResult<T>

    data class Failure(
        val error: ParseError,
    ) : ParsingResult<Nothing>
}

internal inline fun <T> ParsingResult<T>.orReturn(
    onFailure: (ParsingResult.Failure) -> Nothing,
): ParsingResult.Success<T> =
    when (this) {
        is ParsingResult.Success -> this
        is ParsingResult.Failure -> onFailure(this)
    }
