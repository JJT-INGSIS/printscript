package printscript.linter.internal

import printscript.linter.internal.rule.RuleSet
import printscript.statement.StatementReadResult
import printscript.statement.StatementSource

/**
 * Busca la próxima sentencia que incumpla alguna regla. Lee de a una y
 * solo hasta encontrarla: un programa largo no se lee entero.
 */
internal class DiagnosticSearch(
    private val rules: RuleSet,
) {

    /**
     * Total: toda lectura terminal —fin de entrada o error— da un
     * resultado, así que la búsqueda siempre encuentra uno.
     */
    fun findNext(statements: StatementSource): DiagnosticSearchResult {
        return statementReads(statements)
            .firstNotNullOf { readResult -> outcomeOf(readResult) }
    }

    private fun statementReads(statements: StatementSource): Sequence<StatementReadResult> {
        return generateSequence(statements.nextStatement()) { previous ->
            continuationOf(previous)
        }
    }

    private fun continuationOf(previous: StatementReadResult): StatementReadResult? {
        return when (previous) {
            is StatementReadResult.Success -> previous.remainingSource.nextStatement()

            is StatementReadResult.Failure -> null

            StatementReadResult.EndOfInput -> null
        }
    }

    private fun outcomeOf(readResult: StatementReadResult): DiagnosticSearchResult? {
        return when (readResult) {
            StatementReadResult.EndOfInput ->
                DiagnosticSearchResult.Exhausted

            is StatementReadResult.Failure ->
                DiagnosticSearchResult.ParseFailed(readResult.error)

            is StatementReadResult.Success -> foundIn(readResult)
        }
    }

    /**
     * Null significa "seguí buscando": esta sentencia no incumple nada.
     */
    private fun foundIn(readResult: StatementReadResult.Success): DiagnosticSearchResult? {
        return rules.inspect(readResult.statement)
            .takeIf { diagnostics -> diagnostics.isNotEmpty() }
            ?.let { diagnostics ->
                DiagnosticSearchResult.Found(
                    diagnostics = diagnostics,
                    remainingStatements = readResult.remainingSource,
                )
            }
    }
}
