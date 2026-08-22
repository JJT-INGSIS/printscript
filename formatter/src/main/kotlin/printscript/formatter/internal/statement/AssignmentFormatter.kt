package printscript.formatter.internal.statement

import printscript.ast.statement.AssignmentStatement
import printscript.ast.statement.Statement
import printscript.formatter.FormattingError
import printscript.formatter.internal.expression.ExpressionFormatter

internal class AssignmentFormatter(
    private val expressionFormatter: ExpressionFormatter,
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
            return unsupportedStatement(statement)
        }

        val formattedExpression =
            expressionFormatter.formatExpression(
                statement.expression,
            )

        return StatementFormattingResult.Success(
            formattedText =
                "${statement.target.value} = $formattedExpression;",
        )
    }

    private fun unsupportedStatement(
        statement: Statement,
    ): StatementFormattingResult.Failure {
        return StatementFormattingResult.Failure(
            error = FormattingError.UnsupportedStatement(
                span = statement.span,
            ),
        )
    }
}