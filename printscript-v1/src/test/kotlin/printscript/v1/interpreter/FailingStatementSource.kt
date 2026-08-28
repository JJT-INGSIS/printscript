package printscript.v1.interpreter

import printscript.statement.ParseError
import printscript.statement.StatementReadResult
import printscript.statement.StatementSource

class FailingStatementSource(
    private val error: ParseError,
) : StatementSource {

    override fun nextStatement(): StatementReadResult {
        return StatementReadResult.Failure(error)
    }
}
