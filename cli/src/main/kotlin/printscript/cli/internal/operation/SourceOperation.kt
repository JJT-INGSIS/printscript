package printscript.cli.internal.operation

import printscript.cli.internal.io.Terminal
import printscript.statement.StatementSource

internal interface SourceOperation {

    fun outcomeFor(statements: StatementSource, terminal: Terminal): OperationOutcome
}
