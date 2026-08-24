package printscript.linter.internal

import printscript.ast.statement.Statement
import printscript.linter.Diagnostic
import printscript.linter.DiagnosticReadResult
import printscript.linter.DiagnosticSource
import printscript.linter.internal.rule.LintRule
import printscript.statement.StatementReadResult
import printscript.statement.StatementSource

private const val DELIVERED_DIAGNOSTIC_COUNT = 1

/**
 * Una sentencia puede incumplir varias reglas, así que la fuente lleva
 * los diagnósticos que todavía no entregó.
 */
internal data class LintingSource(
    private val statements: StatementSource,
    private val rules: List<LintRule>,
    private val pendingDiagnostics: List<Diagnostic>,
) : DiagnosticSource {

    override fun nextDiagnostic(): DiagnosticReadResult {
        return pendingDiagnostics.firstOrNull()
            ?.let { diagnostic -> deliver(diagnostic) }
            ?: inspectUntilDiagnostic()
    }

    private fun deliver(
        diagnostic: Diagnostic,
    ): DiagnosticReadResult {
        return DiagnosticReadResult.Success(
            diagnostic = diagnostic,
            remainingSource = copy(
                pendingDiagnostics = pendingDiagnostics.drop(DELIVERED_DIAGNOSTIC_COUNT),
            ),
        )
    }

    /**
     * Avanza hasta la primera sentencia que incumpla alguna regla. La
     * secuencia es perezosa, así que las sentencias limpias no acumulan
     * stack ni memoria.
     */
    private fun inspectUntilDiagnostic(): DiagnosticReadResult {
        return lintingSteps()
            .filterIsInstance<LintingStep.Finished>()
            .first()
            .result
    }

    private fun lintingSteps(): Sequence<LintingStep> {
        return generateSequence<LintingStep>(
            LintingStep.Pending(statements),
        ) { step ->
            advance(step)
        }
    }

    private fun advance(
        step: LintingStep,
    ): LintingStep? {
        return when (step) {
            is LintingStep.Finished -> null

            is LintingStep.Pending -> inspectNextStatement(step.statements)
        }
    }

    private fun inspectNextStatement(
        statements: StatementSource,
    ): LintingStep {
        return when (val readResult = statements.nextStatement()) {
            StatementReadResult.EndOfInput ->
                LintingStep.Finished(DiagnosticReadResult.EndOfInput)

            is StatementReadResult.Failure ->
                LintingStep.Finished(
                    DiagnosticReadResult.Failure(error = readResult.error),
                )

            is StatementReadResult.Success -> continueAfter(readResult)
        }
    }

    private fun continueAfter(
        readResult: StatementReadResult.Success,
    ): LintingStep {
        val diagnostics = inspect(readResult.statement)

        val first = diagnostics.firstOrNull()
            ?: return LintingStep.Pending(readResult.remainingSource)

        return LintingStep.Finished(
            DiagnosticReadResult.Success(
                diagnostic = first,
                remainingSource = copy(
                    statements = readResult.remainingSource,
                    pendingDiagnostics = diagnostics.drop(DELIVERED_DIAGNOSTIC_COUNT),
                ),
            ),
        )
    }

    private fun inspect(
        statement: Statement,
    ): List<Diagnostic> {
        return rules.flatMap { rule -> rule.inspect(statement) }
    }

    private sealed interface LintingStep {

        data class Pending(
            val statements: StatementSource,
        ) : LintingStep

        data class Finished(
            val result: DiagnosticReadResult,
        ) : LintingStep
    }

    companion object {

        fun initial(
            statements: StatementSource,
            rules: List<LintRule>,
        ): LintingSource {
            return LintingSource(
                statements = statements,
                rules = rules,
                pendingDiagnostics = emptyList(),
            )
        }
    }
}
