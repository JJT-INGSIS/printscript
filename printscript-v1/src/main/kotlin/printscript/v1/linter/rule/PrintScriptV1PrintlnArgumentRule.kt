package printscript.v1.linter.rule

import printscript.ast.expression.Expression
import printscript.ast.statement.PrintlnStatement
import printscript.linter.Diagnostic
import printscript.linter.StatelessLintRule
import printscript.statement.Statement
import printscript.v1.linter.PrintScriptArgumentAcceptance
import printscript.v1.linter.PrintScriptArgumentAcceptancePolicy
import printscript.v1.linter.PrintScriptV1Diagnostic

public class PrintScriptV1PrintlnArgumentRule(
    private val acceptance: PrintScriptArgumentAcceptancePolicy,
) : StatelessLintRule {

    public override fun diagnosticsIn(statement: Statement): List<Diagnostic> {
        return when (statement) {
            is PrintlnStatement -> inspectArgument(statement.argument)

            else -> emptyList()
        }
    }

    private fun inspectArgument(argument: Expression): List<Diagnostic> {
        return when (acceptance.acceptanceOf(argument)) {
            PrintScriptArgumentAcceptance.ACCEPTED -> emptyList()

            PrintScriptArgumentAcceptance.REJECTED -> listOf(
                PrintScriptV1Diagnostic.UnsupportedPrintlnArgument(argument),
            )
        }
    }
}
