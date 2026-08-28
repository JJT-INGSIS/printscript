package printscript.v1.formatter.internal.statement

import printscript.ast.statement.Statement
import printscript.formatter.FormattingError
import printscript.formatter.StatementFormattingResult

internal fun unsupportedStatementFailure(statement: Statement): StatementFormattingResult.Failure {
    return StatementFormattingResult.Failure(
        error = FormattingError.UnsupportedStatement(
            span = statement.span,
        ),
    )
}
