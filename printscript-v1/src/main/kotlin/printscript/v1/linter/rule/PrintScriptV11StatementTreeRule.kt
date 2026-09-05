package printscript.v1.linter.rule

import printscript.ast.statement.BlockStatement
import printscript.ast.statement.IfStatement
import printscript.linter.Diagnostic
import printscript.linter.LintRule
import printscript.linter.RuleInspection
import printscript.statement.Statement

internal class PrintScriptV11StatementTreeRule(
    rules: List<LintRule>,
) : LintRule {

    private val rules: List<LintRule> = rules.toList()

    override fun inspect(statement: Statement): RuleInspection {
        val (diagnostics, resultingRules) = inspect(statement, rules)

        return RuleInspection(
            diagnostics = diagnostics,
            resultingRule = PrintScriptV11StatementTreeRule(rules = resultingRules),
        )
    }

    private fun inspect(statement: Statement, currentRules: List<LintRule>): Pair<List<Diagnostic>, List<LintRule>> {
        val inspections = currentRules.map { rule -> rule.inspect(statement) }
        val ownDiagnostics = inspections.flatMap { inspection -> inspection.diagnostics }
        val updatedRules = inspections.map { inspection -> inspection.resultingRule }

        val (nestedDiagnostics, finalRules) = when (statement) {
            is IfStatement -> inspectBranches(statement, updatedRules)
            else -> emptyList<Diagnostic>() to updatedRules
        }

        return (ownDiagnostics + nestedDiagnostics) to finalRules
    }

    private fun inspectBranches(
        statement: IfStatement,
        currentRules: List<LintRule>,
    ): Pair<List<Diagnostic>, List<LintRule>> {
        val (thenDiagnostics, rulesAfterThen) = inspectBlock(statement.thenBranch, currentRules)
        val elseBranch = statement.elseBranch
            ?: return thenDiagnostics to rulesAfterThen

        val (elseDiagnostics, rulesAfterElse) = inspectBlock(elseBranch, rulesAfterThen)

        return (thenDiagnostics + elseDiagnostics) to rulesAfterElse
    }

    private fun inspectBlock(
        block: BlockStatement,
        currentRules: List<LintRule>,
    ): Pair<List<Diagnostic>, List<LintRule>> {
        return block.statements.fold(emptyList<Diagnostic>() to currentRules) { (diagnostics, rules), nested ->
            val (nestedDiagnostics, updatedRules) = inspect(nested, rules)

            (diagnostics + nestedDiagnostics) to updatedRules
        }
    }
}
