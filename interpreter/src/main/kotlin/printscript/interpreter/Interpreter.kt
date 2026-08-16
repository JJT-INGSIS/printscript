package printscript.interpreter

import printscript.statement.StatementSource

interface Interpreter {

    fun interpret(
        source: StatementSource,
    ): InterpretationResult
}