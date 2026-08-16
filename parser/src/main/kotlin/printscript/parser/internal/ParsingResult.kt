package printscript.parser.internal

import printscript.statement.ParseError

internal sealed interface ParsingResult<out T> {

    data class Success<T>(
        val value: T,
    ) : ParsingResult<T>

    data class Failure(
        val error: ParseError,
    ) : ParsingResult<Nothing>
}

internal inline fun <T> ParsingResult<T>.orReturn(
    onFailure: (ParsingResult.Failure) -> Nothing,
): T =
    when (this) {
        is ParsingResult.Success -> value
        is ParsingResult.Failure -> onFailure(this)
    }