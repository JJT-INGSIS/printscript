package printscript.v1.formatter.internal.statement

import printscript.ast.statement.AssignmentStatement
import printscript.formatter.StatementFormatter
import printscript.formatter.StatementFormattingResult
import printscript.statement.Statement
import printscript.v1.formatter.internal.ASSIGNMENT_OPERATOR
import printscript.v1.formatter.internal.SEMICOLON
import printscript.v1.formatter.internal.expression.ExpressionFormatter
import printscript.v1.formatter.internal.spaceIfEnabled

internal class AssignmentFormatter(
    private val expressionFormatter: ExpressionFormatter,
    private val insertSpaceAroundEqualsOperator: Boolean,
) : StatementFormatter {

    override fun supportsStatement(statement: Statement): Boolean {
        return statement is AssignmentStatement
    }

    override fun formatStatement(statement: Statement): StatementFormattingResult {
        if (statement !is AssignmentStatement) {
            return unsupportedStatementFailure(statement)
        }

        return StatementFormattingResult.Success(
            formattedText = formatAssignment(statement),
        )
    }

    private fun formatAssignment(statement: AssignmentStatement): String {
        val formattedExpression = expressionFormatter.formatExpression(statement.expression)
        val operatorSpacing = spaceIfEnabled(insertSpaceAroundEqualsOperator)

        return statement.target.value +
            "$operatorSpacing$ASSIGNMENT_OPERATOR" +
            "$operatorSpacing$formattedExpression$SEMICOLON"
    }
}
