package printscript.interpreter

import printscript.interpreter.internal.ConfigurableInterpreter

public object InterpreterFactory {

    public fun <S> create(initialState: S, statementExecutors: List<StatementExecutor<S>>): Interpreter {
        return ConfigurableInterpreter(
            initialState = initialState,
            statementExecutors = statementExecutors,
        )
    }
}
