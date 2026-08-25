package printscript.interpreter

import printscript.statement.StatementSource

public interface Interpreter {

    public fun interpret(source: StatementSource): InterpretationResult
}
