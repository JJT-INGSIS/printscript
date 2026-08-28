package printscript.linter.internal

import printscript.linter.Diagnostic
import printscript.linter.DiagnosticReadResult
import printscript.linter.DiagnosticSource
import printscript.linter.LintRule
import printscript.statement.StatementSource

private const val DELIVERED_DIAGNOSTIC_COUNT = 1

/**
 * Entrega los diagnósticos de a uno. Una sentencia puede incumplir varias
 * reglas, así que la fuente lleva los que todavía no entregó.
 *
 * También lleva la regla: es la que quedó después de mirar lo ya leído, y
 * es con la que se va a mirar lo que falta.
 */
internal data class LintingSource(
    private val statements: StatementSource,
    private val rule: LintRule,
    private val search: DiagnosticSearch,
    private val pendingDiagnostics: List<Diagnostic>,
) : DiagnosticSource {

    override fun nextDiagnostic(): DiagnosticReadResult {
        return pendingDiagnostics.firstOrNull()
            ?.let { diagnostic -> deliverPending(diagnostic) }
            ?: deliverNextFound()
    }

    private fun deliverPending(diagnostic: Diagnostic): DiagnosticReadResult {
        return deliver(
            diagnostic = diagnostic,
            remainingStatements = statements,
            remainingRule = rule,
            remainingDiagnostics = pendingDiagnostics.drop(DELIVERED_DIAGNOSTIC_COUNT),
        )
    }

    private fun deliverNextFound(): DiagnosticReadResult {
        return when (val result = search.findNext(statements, rule)) {
            DiagnosticSearchResult.Exhausted ->
                DiagnosticReadResult.EndOfInput

            is DiagnosticSearchResult.ParseFailed ->
                DiagnosticReadResult.Failure(result.error)

            is DiagnosticSearchResult.Found -> deliverFirstOf(result)
        }
    }

    private fun deliverFirstOf(result: DiagnosticSearchResult.Found): DiagnosticReadResult {
        return deliver(
            diagnostic = result.diagnostics.first(),
            remainingStatements = result.remainingStatements,
            remainingRule = result.resultingRule,
            remainingDiagnostics = result.diagnostics.drop(DELIVERED_DIAGNOSTIC_COUNT),
        )
    }

    private fun deliver(
        diagnostic: Diagnostic,
        remainingStatements: StatementSource,
        remainingRule: LintRule,
        remainingDiagnostics: List<Diagnostic>,
    ): DiagnosticReadResult {
        return DiagnosticReadResult.Success(
            diagnostic = diagnostic,
            remainingSource = copy(
                statements = remainingStatements,
                rule = remainingRule,
                pendingDiagnostics = remainingDiagnostics,
            ),
        )
    }

    companion object {

        fun initial(statements: StatementSource, rule: LintRule, search: DiagnosticSearch): LintingSource {
            return LintingSource(
                statements = statements,
                rule = rule,
                search = search,
                pendingDiagnostics = emptyList(),
            )
        }
    }
}
