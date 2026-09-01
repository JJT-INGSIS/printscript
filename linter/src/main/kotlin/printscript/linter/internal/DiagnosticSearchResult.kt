package printscript.linter.internal

import printscript.linter.Diagnostic
import printscript.linter.LintRule
import printscript.statement.ParseError
import printscript.statement.StatementSource

internal sealed interface DiagnosticSearchResult {

    data class Found(
        val diagnostics: List<Diagnostic>,
        val remainingStatements: StatementSource,
        val resultingRule: LintRule,
    ) : DiagnosticSearchResult

    data class ParseFailed(
        val error: ParseError,
    ) : DiagnosticSearchResult

    data object Exhausted : DiagnosticSearchResult
}
