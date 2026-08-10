package printscript.interpreter

import printscript.model.ast.statement.Statement
import printscript.statement.StatementReadResult
import printscript.statement.StatementSource

class ListStatementSource(
    private val statements: List<Statement>
) : StatementSource {

    private var index = 0

    override fun nextStatement(): StatementReadResult {
        if (index >= statements.size) {
            return StatementReadResult.EndOfInput
        }

        val statement = statements[index]
        index = index + 1
        return StatementReadResult.Success(statement)
    }
}