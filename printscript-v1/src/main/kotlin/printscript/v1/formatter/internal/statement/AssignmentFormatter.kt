package printscript.v1.formatter.internal.statement

import printscript.ast.statement.AssignmentStatement
import printscript.formatter.StatementFormatter
import printscript.formatter.StatementFormattingContext
import printscript.formatter.StatementFormattingResult
import printscript.statement.Statement
import printscript.v1.formatter.internal.expression.ExpressionFormatter
import printscript.v1.formatter.internal.spaceIfEnabled
import printscript.v1.internal.PrintScriptV1Lexemes.ASSIGNMENT_OPERATOR
import printscript.v1.internal.PrintScriptV1Lexemes.SEMICOLON

internal class AssignmentFormatter(
    private val expressionFormatter: ExpressionFormatter,
    private val insertSpaceAroundEqualsOperator: Boolean,
) : StatementFormatter {

    override fun supportsStatement(statement: Statement): Boolean {
        return statement is AssignmentStatement
    }

    override fun formatStatement(
        statement: Statement,
        context: StatementFormattingContext,
    ): StatementFormattingResult {
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
