package printscript.v1.linter.rule

import printscript.ast.statement.BlockStatement
import printscript.ast.statement.IfStatement
import printscript.linter.CompositeRule
import printscript.linter.Diagnostic
import printscript.linter.LintRule
import printscript.linter.RuleInspection
import printscript.statement.Statement

internal class PrintScriptV11RecursiveStatementRule private constructor(
    private val delegate: LintRule,
) : LintRule {

    constructor(rules: List<LintRule>) : this(delegate = CompositeRule(rules))

    override fun inspect(statement: Statement): RuleInspection {
        val (diagnostics, resultingDelegate) = inspect(statement, delegate)

        return RuleInspection(
            diagnostics = diagnostics,
            resultingRule = PrintScriptV11RecursiveStatementRule(delegate = resultingDelegate),
        )
    }

    private fun inspect(statement: Statement, currentDelegate: LintRule): Pair<List<Diagnostic>, LintRule> {
        val inspection = currentDelegate.inspect(statement)

        val (nestedDiagnostics, finalDelegate) = when (statement) {
            is IfStatement -> inspectBranches(statement, inspection.resultingRule)
            else -> emptyList<Diagnostic>() to inspection.resultingRule
        }

        return (inspection.diagnostics + nestedDiagnostics) to finalDelegate
    }

    private fun inspectBranches(statement: IfStatement, currentDelegate: LintRule): Pair<List<Diagnostic>, LintRule> {
        val (thenDiagnostics, delegateAfterThen) = inspectBlock(statement.thenBranch, currentDelegate)
        val elseBranch = statement.elseBranch
            ?: return thenDiagnostics to delegateAfterThen

        val (elseDiagnostics, delegateAfterElse) = inspectBlock(elseBranch, delegateAfterThen)

        return (thenDiagnostics + elseDiagnostics) to delegateAfterElse
    }

    private fun inspectBlock(block: BlockStatement, currentDelegate: LintRule): Pair<List<Diagnostic>, LintRule> {
        return block.statements.fold(emptyList<Diagnostic>() to currentDelegate) { (diagnostics, delegate), nested ->
            val (nestedDiagnostics, updatedDelegate) = inspect(nested, delegate)

            (diagnostics + nestedDiagnostics) to updatedDelegate
        }
    }
}
