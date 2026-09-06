package printscript.v1.linter.rule

import printscript.ast.expression.Expression
import printscript.ast.statement.AssignmentStatement
import printscript.ast.statement.PrintlnStatement
import printscript.ast.statement.VariableDeclarationStatement
import printscript.linter.Diagnostic
import printscript.linter.StatelessLintRule
import printscript.statement.Statement
import printscript.v1.linter.PrintScriptV1ArgumentAcceptance
import printscript.v1.linter.PrintScriptV1ArgumentAcceptancePolicy
import printscript.v1.linter.PrintScriptV1Diagnostic
import printscript.v1.linter.PrintScriptV1ExpressionKind

public class PrintScriptV1PrintlnArgumentRule(
    acceptanceByKind: Map<PrintScriptV1ExpressionKind, PrintScriptV1ArgumentAcceptance>,
) : StatelessLintRule() {

    private val acceptancePolicy = PrintScriptV1ArgumentAcceptancePolicy(acceptanceByKind)

    protected override fun diagnosticsIn(statement: Statement): List<Diagnostic> {
        return when (statement) {
            is PrintlnStatement -> inspectArgument(statement.argument)

            is VariableDeclarationStatement -> emptyList()

            is AssignmentStatement -> emptyList()

            else -> emptyList()
        }
    }

    private fun inspectArgument(argument: Expression): List<Diagnostic> {
        return when (acceptancePolicy.acceptanceOf(argument)) {
            PrintScriptV1ArgumentAcceptance.ACCEPTED -> emptyList()

            PrintScriptV1ArgumentAcceptance.REJECTED -> listOf(
                PrintScriptV1Diagnostic.UnsupportedPrintlnArgument(argument),
            )
        }
    }
}
