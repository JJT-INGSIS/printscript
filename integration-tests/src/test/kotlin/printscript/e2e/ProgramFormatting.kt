package printscript.e2e

import printscript.formatter.FormattingError

internal sealed interface ProgramFormatting {

    data class Success(val formattedText: String) : ProgramFormatting

    data class Failure(val error: FormattingError) : ProgramFormatting
}
