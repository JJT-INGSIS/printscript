package printscript.parser.internal

internal inline fun <T> ParsingResult<T>.orReturn(
    onFailure: (ParsingResult.Failure) -> Nothing,
): T =
    when (this) {
        is ParsingResult.Success -> value
        is ParsingResult.Failure -> onFailure(this)
    }
