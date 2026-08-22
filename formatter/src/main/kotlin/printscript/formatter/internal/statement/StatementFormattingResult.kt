package printscript.formatter.internal.statement

import printscript.formatter.FormattingError

internal sealed interface StatementFormattingResult {

    data class Success(
        val formattedText: String,
    ) : StatementFormattingResult

    data class Failure(
        val error: FormattingError,
    ) : StatementFormattingResult
}