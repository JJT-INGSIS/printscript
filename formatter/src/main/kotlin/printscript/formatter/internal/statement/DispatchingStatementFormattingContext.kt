package printscript.formatter.internal.statement

import printscript.formatter.StatementFormattingContext
import printscript.formatter.StatementFormattingResult
import printscript.formatter.StatementSeparationPolicy
import printscript.statement.Statement

internal class DispatchingStatementFormattingContext(
    private val dispatcher: StatementFormatterDispatcher,
    private val statementSeparationPolicy: StatementSeparationPolicy,
) : StatementFormattingContext {

    override fun formatStatement(statement: Statement): StatementFormattingResult {
        return dispatcher.formatStatement(
            statement = statement,
            context = this,
        )
    }

    override fun formatStatements(statements: List<Statement>): StatementFormattingResult {
        var formattedText = ""
        var hasPreviousStatement = false

        for (statement in statements) {
            when (val result = formatStatement(statement)) {
                is StatementFormattingResult.Failure -> return result
                is StatementFormattingResult.Success -> {
                    val separator = statementSeparationPolicy.separatorBeforeStatement(
                        statement = statement,
                        hasPreviousStatement = hasPreviousStatement,
                    )
                    formattedText += separator + result.formattedText
                    hasPreviousStatement = true
                }
            }
        }

        return StatementFormattingResult.Success(formattedText)
    }
}
