package printscript.formatter.internal.statement

import printscript.ast.statement.AssignmentStatement
import printscript.ast.statement.Statement
import printscript.formatter.internal.expression.ExpressionFormatter

internal class AssignmentFormatter(
    private val expressionFormatter: ExpressionFormatter,
    private val insertSpaceAroundEqualsOperator: Boolean,
) : StatementFormatter {

    override fun supportsStatement(
        statement: Statement,
    ): Boolean {
        return statement is AssignmentStatement
    }

    override fun formatStatement(
        statement: Statement,
    ): StatementFormattingResult {
        if (statement !is AssignmentStatement) {
            return createUnsupportedStatementFailure(statement)
        }

        return StatementFormattingResult.Success(
            formattedText = formatAssignment(statement),
        )
    }

    private fun formatAssignment(
        statement: AssignmentStatement,
    ): String {
        val formattedExpression =
            expressionFormatter.formatExpression(
                statement.expression,
            )
        val equalsOperatorSpacing =
            spaceIfEnabled(insertSpaceAroundEqualsOperator)

        return statement.target.value +
            "$equalsOperatorSpacing=" +
            "$equalsOperatorSpacing$formattedExpression;"
    }

    private fun spaceIfEnabled(
        enabled: Boolean,
    ): String {
        return if (enabled) {
            " "
        } else {
            ""
        }
    }
}
