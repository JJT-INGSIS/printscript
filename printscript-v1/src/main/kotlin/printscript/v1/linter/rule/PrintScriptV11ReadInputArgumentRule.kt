package printscript.v1.linter.rule

import printscript.ast.expression.BinaryExpression
import printscript.ast.expression.BooleanLiteralExpression
import printscript.ast.expression.Expression
import printscript.ast.expression.GroupingExpression
import printscript.ast.expression.IdentifierExpression
import printscript.ast.expression.NumberLiteralExpression
import printscript.ast.expression.ReadEnvironmentExpression
import printscript.ast.expression.ReadInputExpression
import printscript.ast.expression.StringLiteralExpression
import printscript.ast.expression.UnaryExpression
import printscript.ast.statement.AssignmentStatement
import printscript.ast.statement.PrintlnStatement
import printscript.ast.statement.VariableDeclarationStatement
import printscript.linter.Diagnostic
import printscript.linter.StatelessLintRule
import printscript.statement.Statement
import printscript.v1.linter.PrintScriptArgumentAcceptance
import printscript.v1.linter.PrintScriptArgumentAcceptancePolicy
import printscript.v1.linter.PrintScriptV11Diagnostic

public class PrintScriptV11ReadInputArgumentRule(
    private val acceptance: PrintScriptArgumentAcceptancePolicy,
) : StatelessLintRule {

    public override fun diagnosticsIn(statement: Statement): List<Diagnostic> {
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

            is IdentifierExpression -> emptyList()

            is NumberLiteralExpression -> emptyList()

            is StringLiteralExpression -> emptyList()

            is BooleanLiteralExpression -> emptyList()
        }
    }

    private fun diagnosticsForReadInput(expression: ReadInputExpression): List<Diagnostic> {
        val prompt = expression.prompt

        return diagnosticsIn(prompt) + when (acceptance.acceptanceOf(prompt)) {
            PrintScriptArgumentAcceptance.ACCEPTED -> emptyList()

            PrintScriptArgumentAcceptance.REJECTED -> listOf(
                PrintScriptV11Diagnostic.UnsupportedReadInputArgument(prompt),
            )
        }
    }
}
