package printscript.v1.linter.rule

import printscript.ast.expression.Expression
import printscript.ast.statement.AssignmentStatement
import printscript.ast.statement.PrintlnStatement
import printscript.ast.statement.VariableDeclarationStatement
import printscript.linter.Diagnostic
import printscript.linter.StatelessLintRule
import printscript.statement.Statement
import printscript.v1.linter.PrintScriptV1ArgumentAcceptance
import printscript.v1.linter.PrintScriptV1Diagnostic
import printscript.v1.linter.PrintScriptV1ExpressionKind

/**
 * Limita qué puede ir como argumento de `println`.
 *
 * La regla es el mecanismo y el mapa es la política: invertir qué se
 * acepta no toca esta clase.
 */
public class PrintScriptV1PrintlnArgumentRule(
    acceptanceByKind: Map<PrintScriptV1ExpressionKind, PrintScriptV1ArgumentAcceptance>,
) : StatelessLintRule() {

    private val acceptanceByKind: Map<PrintScriptV1ExpressionKind, PrintScriptV1ArgumentAcceptance> =
        acceptanceByKind.toMap()

    init {
        val uncoveredKinds = PrintScriptV1ExpressionKind.entries - acceptanceByKind.keys

        require(uncoveredKinds.isEmpty()) {
            "La configuración de println no cubre: $uncoveredKinds"
        }
    }

    protected override fun diagnosticsIn(statement: Statement): List<Diagnostic> {
        return when (statement) {
            is PrintlnStatement -> inspectArgument(statement.argument)

            is VariableDeclarationStatement -> emptyList()

            is AssignmentStatement -> emptyList()

            else -> emptyList()
        }
    }

    private fun inspectArgument(argument: Expression): List<Diagnostic> {
        return when (acceptanceOf(argument)) {
            PrintScriptV1ArgumentAcceptance.ACCEPTED -> emptyList()

            PrintScriptV1ArgumentAcceptance.REJECTED -> listOf(
                PrintScriptV1Diagnostic.UnsupportedPrintlnArgument(argument),
            )
        }
    }

    /**
     * Total: el constructor ya garantizó que el mapa cubre toda clase.
     */
    private fun acceptanceOf(argument: Expression): PrintScriptV1ArgumentAcceptance {
        return acceptanceByKind.getValue(
            PrintScriptV1ExpressionKind.of(argument),
        )
    }
}
