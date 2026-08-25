package printscript.linter.internal.rule

import printscript.ast.expression.Expression
import printscript.ast.statement.AssignmentStatement
import printscript.ast.statement.PrintlnStatement
import printscript.ast.statement.Statement
import printscript.ast.statement.VariableDeclarationStatement
import printscript.linter.ArgumentAcceptance
import printscript.linter.Diagnostic
import printscript.linter.ExpressionKind

internal class PrintlnArgumentRule(
    acceptanceByKind: Map<ExpressionKind, ArgumentAcceptance>,
) : LintRule {

    private val acceptanceByKind: Map<ExpressionKind, ArgumentAcceptance> =
        acceptanceByKind.toMap()

    init {
        val uncoveredKinds = ExpressionKind.entries - acceptanceByKind.keys

        require(uncoveredKinds.isEmpty()) {
            "La configuración de println no cubre: $uncoveredKinds"
        }
    }

    override fun inspect(
        statement: Statement,
    ): List<Diagnostic> {
        return when (statement) {
            is PrintlnStatement -> inspectArgument(statement.argument)

            is VariableDeclarationStatement -> emptyList()

            is AssignmentStatement -> emptyList()
        }
    }

    private fun inspectArgument(
        argument: Expression,
    ): List<Diagnostic> {
        return when (acceptanceOf(argument)) {
            ArgumentAcceptance.ACCEPTED -> emptyList()

            ArgumentAcceptance.REJECTED -> listOf(
                Diagnostic.UnsupportedPrintlnArgument(argument),
            )
        }
    }

    /**
     * Total: el constructor ya garantizó que el mapa cubre toda clase.
     */
    private fun acceptanceOf(
        argument: Expression,
    ): ArgumentAcceptance {
        return acceptanceByKind.getValue(
            ExpressionKind.of(argument),
        )
    }
}
