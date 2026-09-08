package printscript.v1.linter.rule

import printscript.ast.statement.BlockStatement
import printscript.ast.statement.IfStatement
import printscript.linter.CompositeRule
import printscript.linter.Diagnostic
import printscript.linter.LintRule
import printscript.linter.RuleInspection
import printscript.statement.Statement

internal class PrintScriptV11StatementTreeRule private constructor(
    private val delegate: LintRule,
) : LintRule {

    constructor(rules: List<LintRule>) : this(delegate = CompositeRule(rules))

    override fun inspect(statement: Statement): RuleInspection {
        val outcome = inspect(statement, delegate)

        return RuleInspection(
            diagnostics = outcome.diagnostics,
            resultingRule = PrintScriptV11StatementTreeRule(delegate = outcome.resultingDelegate),
        )
    }

    private fun inspect(statement: Statement, currentDelegate: LintRule): TreeInspectionOutcome {
        val inspection = currentDelegate.inspect(statement)

        val nestedOutcome = when (statement) {
            is IfStatement -> inspectBranches(statement, inspection.resultingRule)
            else -> TreeInspectionOutcome(diagnostics = emptyList(), resultingDelegate = inspection.resultingRule)
        }

        return TreeInspectionOutcome(
            diagnostics = inspection.diagnostics + nestedOutcome.diagnostics,
            resultingDelegate = nestedOutcome.resultingDelegate,
        )
    }

    private fun inspectBranches(statement: IfStatement, currentDelegate: LintRule): TreeInspectionOutcome {
        val thenOutcome = inspectBlock(statement.thenBranch, currentDelegate)
        val elseBranch = statement.elseBranch
            ?: return thenOutcome

        val elseOutcome = inspectBlock(elseBranch, thenOutcome.resultingDelegate)

        return TreeInspectionOutcome(
            diagnostics = thenOutcome.diagnostics + elseOutcome.diagnostics,
            resultingDelegate = elseOutcome.resultingDelegate,
        )
    }

    private fun inspectBlock(block: BlockStatement, currentDelegate: LintRule): TreeInspectionOutcome {
        return block.statements.fold(
            initial = TreeInspectionOutcome(diagnostics = emptyList(), resultingDelegate = currentDelegate),
        ) { outcome, nested ->
            val nestedOutcome = inspect(nested, outcome.resultingDelegate)

            TreeInspectionOutcome(
                diagnostics = outcome.diagnostics + nestedOutcome.diagnostics,
                resultingDelegate = nestedOutcome.resultingDelegate,
            )
        }
    }

    private data class TreeInspectionOutcome(
        val diagnostics: List<Diagnostic>,
        val resultingDelegate: LintRule,
    )
}
