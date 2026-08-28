package printscript.interpreter

import printscript.interpreter.internal.ConfigurableInterpreter

public object InterpreterFactory {

    /**
     * Creates a lazy interpreter. When several executors support a statement,
     * the first configured executor has priority.
     */
    public fun <S> create(initialState: S, statementExecutors: List<StatementExecutor<S>>): Interpreter {
        return ConfigurableInterpreter(
            initialState = initialState,
            statementExecutors = statementExecutors,
        )
    }
}
