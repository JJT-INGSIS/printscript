package printscript.parser

import printscript.statement.ParseError

public sealed interface ParsingResult<out T> {

    /**
     * The parsed value and the only context that should be used to continue.
     */
    public data class Success<out T>(
        public val value: T,
        public val resultingContext: ParsingContext,
    ) : ParsingResult<T>

    public data class Failure(
        public val error: ParseError,
    ) : ParsingResult<Nothing>
}

public inline fun <T> ParsingResult<T>.orReturn(
    onFailure: (ParsingResult.Failure) -> Nothing,
): ParsingResult.Success<T> = when (this) {
    is ParsingResult.Success -> this
    is ParsingResult.Failure -> onFailure(this)
}
