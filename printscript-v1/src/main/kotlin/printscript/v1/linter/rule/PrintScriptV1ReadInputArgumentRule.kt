package printscript.v1.linter.rule

import printscript.ast.expression.BinaryExpression
import printscript.ast.expression.Expression
import printscript.ast.expression.GroupingExpression
import printscript.ast.expression.ReadEnvironmentExpression
import printscript.ast.expression.ReadInputExpression
import printscript.ast.expression.UnaryExpression
import printscript.ast.statement.AssignmentStatement
import printscript.ast.statement.PrintlnStatement
import printscript.ast.statement.VariableDeclarationStatement
import printscript.linter.Diagnostic
import printscript.linter.StatelessLintRule
import printscript.statement.Statement
import printscript.v1.linter.PrintScriptV11Diagnostic
import printscript.v1.linter.PrintScriptV1ArgumentAcceptance
import printscript.v1.linter.PrintScriptV1ExpressionKind

public class PrintScriptV1ReadInputArgumentRule(
    acceptanceByKind: Map<PrintScriptV1ExpressionKind, PrintScriptV1ArgumentAcceptance>,
) : StatelessLintRule() {

    private val acceptanceByKind: Map<PrintScriptV1ExpressionKind, PrintScriptV1ArgumentAcceptance> =
        acceptanceByKind.toMap()

    init {
        val uncoveredKinds = PrintScriptV1ExpressionKind.entries - acceptanceByKind.keys

        require(uncoveredKinds.isEmpty()) {
            "La configuración de readInput no cubre: $uncoveredKinds"
        }
    }

    protected override fun diagnosticsIn(statement: Statement): List<Diagnostic> {
        return expressionsIn(statement).flatMap { expression -> diagnosticsIn(expression) }
    }

    private fun expressionsIn(statement: Statement): List<Expression> {
        return when (statement) {
            is VariableDeclarationStatement -> listOfNotNull(statement.initializer)

            is AssignmentStatement -> listOf(statement.expression)

            is PrintlnStatement -> listOf(statement.argument)

            else -> emptyList()
        }
    }

    private fun diagnosticsIn(expression: Expression): List<Diagnostic> {
        return when (expression) {
            is ReadInputExpression -> diagnosticsForReadInput(expression)

            is BinaryExpression -> diagnosticsIn(expression.left) + diagnosticsIn(expression.right)

            is UnaryExpression -> diagnosticsIn(expression.operand)

            is GroupingExpression -> diagnosticsIn(expression.expression)

            is ReadEnvironmentExpression -> diagnosticsIn(expression.variableName)

            else -> emptyList()
        }
    }

    private fun diagnosticsForReadInput(expression: ReadInputExpression): List<Diagnostic> {
        val prompt = expression.prompt

        return diagnosticsIn(prompt) + when (acceptanceOf(prompt)) {
            PrintScriptV1ArgumentAcceptance.ACCEPTED -> emptyList()

            PrintScriptV1ArgumentAcceptance.REJECTED -> listOf(
                PrintScriptV11Diagnostic.UnsupportedReadInputArgument(prompt),
            )
        }
    }

    private fun acceptanceOf(expression: Expression): PrintScriptV1ArgumentAcceptance {
        return acceptanceByKind.getValue(
            PrintScriptV1ExpressionKind.of(expression),
        )
    }
}
