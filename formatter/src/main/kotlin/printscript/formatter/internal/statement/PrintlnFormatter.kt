package printscript.formatter.internal.statement

import printscript.ast.statement.PrintlnStatement
import printscript.ast.statement.Statement
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
            return createUnsupportedStatementFailure(statement)
        }

        return StatementFormattingResult.Success(
            formattedText = formatPrintln(statement),
        )
    }

    private fun formatPrintln(
        statement: PrintlnStatement,
    ): String {
        val formattedArgument =
            expressionFormatter.formatExpression(
                statement.argument,
            )

        return "println($formattedArgument);"
    }
}
