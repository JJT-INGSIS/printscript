package printscript.formatter.internal.statement

import printscript.ast.statement.PrintlnStatement
import printscript.ast.statement.Statement
import printscript.formatter.FormattingError
import printscript.formatter.internal.expression.ExpressionFormatter

internal class PrintlnFormatter(
    private val expressionFormatter: ExpressionFormatter,
) : StatementFormatter {

    override fun supportsStatement(
        statement: Statement,
    ): Boolean {
        return statement is PrintlnStatement
    }

    override fun formatStatement(
        statement: Statement,
    ): StatementFormattingResult {
        if (statement !is PrintlnStatement) {
            return unsupportedStatement(statement)
        }

        val formattedArgument =
            expressionFormatter.formatExpression(
                statement.argument,
            )

        return StatementFormattingResult.Success(
            formattedText = "println($formattedArgument);",
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