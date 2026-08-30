package printscript.v1.formatter.internal.statement

import printscript.ast.statement.PrintlnStatement
import printscript.formatter.StatementFormatter
import printscript.formatter.StatementFormattingResult
import printscript.statement.Statement
import printscript.v1.formatter.internal.LEFT_PARENTHESIS
import printscript.v1.formatter.internal.OUTPUT_FUNCTION_NAME
import printscript.v1.formatter.internal.RIGHT_PARENTHESIS
import printscript.v1.formatter.internal.SEMICOLON
import printscript.v1.formatter.internal.expression.ExpressionFormatter

internal class PrintlnFormatter(
    private val expressionFormatter: ExpressionFormatter,
) : StatementFormatter {

    override fun supportsStatement(statement: Statement): Boolean {
        return statement is PrintlnStatement
    }

    override fun formatStatement(statement: Statement): StatementFormattingResult {
        if (statement !is PrintlnStatement) {
            return unsupportedStatementFailure(statement)
        }

        return StatementFormattingResult.Success(
            formattedText = formatPrintln(statement),
        )
    }

    private fun formatPrintln(statement: PrintlnStatement): String {
        val formattedArgument = expressionFormatter.formatExpression(statement.argument)

        return "$OUTPUT_FUNCTION_NAME$LEFT_PARENTHESIS" +
            "$formattedArgument$RIGHT_PARENTHESIS$SEMICOLON"
    }
}
