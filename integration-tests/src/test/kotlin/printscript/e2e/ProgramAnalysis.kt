package printscript.e2e

import printscript.linter.Diagnostic
import printscript.statement.ParseError

internal sealed interface ProgramAnalysis {

    data class Success(val diagnostics: List<Diagnostic>) : ProgramAnalysis

    data class Failure(val error: ParseError) : ProgramAnalysis
}
