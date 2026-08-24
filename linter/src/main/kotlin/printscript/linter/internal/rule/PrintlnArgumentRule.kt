package printscript.linter.internal.rule

import printscript.ast.expression.BinaryExpression
import printscript.ast.expression.Expression
import printscript.ast.expression.GroupingExpression
import printscript.ast.expression.IdentifierExpression
import printscript.ast.expression.NumberLiteralExpression
import printscript.ast.expression.StringLiteralExpression
import printscript.ast.expression.UnaryExpression
import printscript.ast.statement.AssignmentStatement
import printscript.ast.statement.PrintlnStatement
import printscript.ast.statement.Statement
import printscript.ast.statement.VariableDeclarationStatement
import printscript.linter.Diagnostic

internal class PrintlnArgumentRule : LintRule {

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
        return when (acceptsArgument(argument)) {
            true -> emptyList()

            false -> listOf(Diagnostic.UnsupportedPrintlnArgument(argument))
        }
    }

    /**
     * `println` solo acepta una variable o un literal: nada de
     * expresiones armadas dentro de la llamada.
     */
    private fun acceptsArgument(
        argument: Expression,
    ): Boolean {
        return when (argument) {
            is IdentifierExpression -> true

            is NumberLiteralExpression -> true

            is StringLiteralExpression -> true

            is BinaryExpression -> false

            is UnaryExpression -> false

            is GroupingExpression -> false
        }
    }
}
