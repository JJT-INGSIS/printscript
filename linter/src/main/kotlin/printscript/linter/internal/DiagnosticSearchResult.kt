package printscript.linter.internal

import printscript.linter.Diagnostic
import printscript.statement.ParseError
import printscript.statement.StatementSource

internal sealed interface DiagnosticSearchResult {

    /**
     * Lo que incumple la sentencia encontrada, con lo que quedó por leer
     * detrás de ella.
     */
    data class Found(
        val diagnostics: List<Diagnostic>,
        val remainingStatements: StatementSource,
    ) : DiagnosticSearchResult

    data class ParseFailed(
        val error: ParseError,
    ) : DiagnosticSearchResult

    data object Exhausted : DiagnosticSearchResult
}
