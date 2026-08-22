package printscript.formatter.internal

import printscript.ast.statement.Statement
import printscript.formatter.FormattedSource
import printscript.formatter.FormattedStatementReadResult
import printscript.formatter.FormattingError
import printscript.formatter.internal.separation.StatementSeparationPolicy
import printscript.formatter.internal.statement.StatementFormatterDispatcher
import printscript.formatter.internal.statement.StatementFormattingResult
import printscript.statement.StatementReadResult
import printscript.statement.StatementSource

internal data class LazyFormattedSource(
    private val statementSource: StatementSource,
    private val statementFormatterDispatcher:
    StatementFormatterDispatcher,
    private val separationPolicy: StatementSeparationPolicy,
    private val hasPreviousStatement: Boolean,
) : FormattedSource {

    override fun nextFormattedStatement():
        FormattedStatementReadResult {
        return when (
            val statementReadResult =
                statementSource.nextStatement()
        ) {
            is StatementReadResult.Success ->
                formatReadStatement(statementReadResult)

            is StatementReadResult.Failure ->
                createParseFailure(statementReadResult)

            StatementReadResult.EndOfInput ->
                FormattedStatementReadResult.EndOfInput
        }
    }

    private fun formatReadStatement(
        statementReadResult: StatementReadResult.Success,
    ): FormattedStatementReadResult {
        return when (
            val formattingResult =
                statementFormatterDispatcher.formatStatement(
                    statementReadResult.statement,
                )
        ) {
            is StatementFormattingResult.Success ->
                createFormattedStatementSuccess(
                    statement = statementReadResult.statement,
                    formattedText =
                        formattingResult.formattedText,
                    remainingSource =
                        statementReadResult.remainingSource,
                )

            is StatementFormattingResult.Failure ->
                FormattedStatementReadResult.Failure(
                    error = formattingResult.error,
                )
        }
    }

    private fun createFormattedStatementSuccess(
        statement: Statement,
        formattedText: String,
        remainingSource: StatementSource,
    ): FormattedStatementReadResult.Success {
        val statementSeparator =
            separationPolicy.separatorBefore(
                statement = statement,
                hasPreviousStatement =
                    hasPreviousStatement,
            )

        return FormattedStatementReadResult.Success(
            formattedText = "$statementSeparator$formattedText",
            remainingSource =
                copy(
                    statementSource = remainingSource,
                    hasPreviousStatement = true,
                ),
        )
    }

    private fun createParseFailure(
        statementReadResult: StatementReadResult.Failure,
    ): FormattedStatementReadResult.Failure {
        return FormattedStatementReadResult.Failure(
            error = FormattingError.ParseFailure(
                error = statementReadResult.error,
            ),
        )
    }
}
