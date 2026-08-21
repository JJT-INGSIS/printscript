package printscript.interpreter

import printscript.ast.statement.Statement
import printscript.statement.StatementReadResult
import printscript.statement.StatementSource

private const val CONSUMED_STATEMENT_COUNT = 1

class ListStatementSource(
    private val statements: List<Statement>,
) : StatementSource {

    override fun nextStatement(): StatementReadResult {
        val statement = statements.firstOrNull()
            ?: return StatementReadResult.EndOfInput

        return StatementReadResult.Success(
            statement = statement,
            remainingSource = ListStatementSource(
                statements = statements.drop(CONSUMED_STATEMENT_COUNT),
            ),
        )
    }
}
