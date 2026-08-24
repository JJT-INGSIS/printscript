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
    private val statementFormatterDispatcher: StatementFormatterDispatcher,
    private val statementSeparationPolicy: StatementSeparationPolicy,
    private val hasPreviousStatement: Boolean,
) : FormattedSource {

    override fun nextFormattedStatement(): FormattedStatementReadResult {
        return when (
            val statementReadResult =
                statementSource.nextStatement()
        ) {
            is StatementReadResult.Success ->
                formatStatementReadSuccess(statementReadResult)

            is StatementReadResult.Failure ->
                createParseFailure(statementReadResult)

            StatementReadResult.EndOfInput ->
                FormattedStatementReadResult.EndOfInput
        }
    }

    private fun formatStatementReadSuccess(
        statementReadResult: StatementReadResult.Success,
    ): FormattedStatementReadResult {
        return when (
            val statementFormattingResult =
                statementFormatterDispatcher.formatStatement(
                    statementReadResult.statement,
                )
        ) {
            is StatementFormattingResult.Success ->
                createFormattedStatementReadSuccess(
                    statement = statementReadResult.statement,
                    formattedText =
                        statementFormattingResult.formattedText,
                    remainingStatementSource =
                        statementReadResult.remainingSource,
                )

            is StatementFormattingResult.Failure ->
                FormattedStatementReadResult.Failure(
                    error = statementFormattingResult.error,
                )
        }
    }

    private fun createFormattedStatementReadSuccess(
        statement: Statement,
        formattedText: String,
        remainingStatementSource: StatementSource,
    ): FormattedStatementReadResult.Success {
        val separatorBeforeStatement =
            statementSeparationPolicy.separatorBeforeStatement(
                statement = statement,
                hasPreviousStatement =
                    hasPreviousStatement,
            )

        return FormattedStatementReadResult.Success(
            formattedText = "$separatorBeforeStatement$formattedText",
            remainingSource =
                copy(
                    statementSource = remainingStatementSource,
                    hasPreviousStatement = true,
                ),
        )
    }

    private fun createParseFailure(
        statementReadResult: StatementReadResult.Failure,
    ): FormattedStatementReadResult.Failure {
        return FormattedStatementReadResult.Failure(
            error = FormattingError.ParseFailure(
                parseError = statementReadResult.error,
            ),
        )
    }
}
