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