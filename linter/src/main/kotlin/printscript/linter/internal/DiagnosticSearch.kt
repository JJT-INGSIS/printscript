package printscript.linter.internal

import printscript.linter.LintRule
import printscript.linter.RuleInspection
import printscript.statement.StatementReadResult
import printscript.statement.StatementSource

/**
 * Busca la próxima sentencia que incumpla alguna regla. Lee de a una y
 * solo hasta encontrarla: un programa largo no se lee entero.
 */
internal class DiagnosticSearch {

    /**
     * Total: toda lectura terminal —fin de entrada o error— da un
     * resultado, así que la búsqueda siempre encuentra uno.
     *
     * Recursiva por cola: avanza sin apilar, y la regla con la que sigue
     * sale de haber inspeccionado la sentencia anterior.
     */
    tailrec fun findNext(statements: StatementSource, rule: LintRule): DiagnosticSearchResult {
        return when (val readResult = statements.nextStatement()) {
            StatementReadResult.EndOfInput ->
                DiagnosticSearchResult.Exhausted

            is StatementReadResult.Failure ->
                DiagnosticSearchResult.ParseFailed(readResult.error)

            is StatementReadResult.Success -> {
                val inspection = rule.inspect(readResult.statement)

                if (inspection.diagnostics.isNotEmpty()) {
                    return foundIn(readResult, inspection)
                }

                findNext(
                    statements = readResult.remainingSource,
                    rule = inspection.resultingRule,
                )
            }
        }
    }

    private fun foundIn(read: StatementReadResult.Success, inspection: RuleInspection): DiagnosticSearchResult {
        return DiagnosticSearchResult.Found(
            diagnostics = inspection.diagnostics,
            remainingStatements = read.remainingSource,
            resultingRule = inspection.resultingRule,
        )
    }
}
