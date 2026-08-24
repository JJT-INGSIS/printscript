package printscript.formatter.internal.statement

import printscript.ast.statement.Statement
import printscript.formatter.FormattingError

internal sealed interface StatementFormattingResult {

    data class Success(
        val formattedText: String,
    ) : StatementFormattingResult

    data class Failure(
        val error: FormattingError,
    ) : StatementFormattingResult
}

internal fun createUnsupportedStatementFailure(statement: Statement): StatementFormattingResult.Failure {
    return StatementFormattingResult.Failure(
        error = FormattingError.UnsupportedStatement(
            span = statement.span,
        ),
    )
}
