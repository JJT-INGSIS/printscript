package printscript.interpreter.internal

import printscript.interpreter.ExecutionResult
import printscript.interpreter.InterpretationResult
import printscript.interpreter.Interpreter
import printscript.interpreter.StatementExecutionContext
import printscript.interpreter.StatementExecutor
import printscript.statement.StatementReadResult
import printscript.statement.StatementSource

internal class ConfigurableInterpreter<S>(
    initialState: S,
    statementExecutors: List<StatementExecutor<S>>,
) : Interpreter {

    private val initialContext: StatementExecutionContext<S> = DispatchingStatementExecutionContext(
        dispatcher = StatementExecutorDispatcher(statementExecutors = statementExecutors),
        state = initialState,
    )

    override fun interpret(source: StatementSource): InterpretationResult {
        return interpretRemainingStatements(
            source = source,
            context = initialContext,
        )
    }

    private tailrec fun interpretRemainingStatements(
        source: StatementSource,
        context: StatementExecutionContext<S>,
    ): InterpretationResult {
        return when (val readResult = source.nextStatement()) {
            StatementReadResult.EndOfInput ->
                InterpretationResult.Success

            is StatementReadResult.Failure ->
                InterpretationResult.ParseFailure(readResult.error)

            is StatementReadResult.Success ->
                when (val execution = context.executeStatement(readResult.statement)) {
                    is ExecutionResult.Failure ->
                        InterpretationResult.SemanticFailure(execution.error)

                    is ExecutionResult.Success ->
                        interpretRemainingStatements(
                            source = readResult.remainingSource,
                            context = context.withState(execution.value),
                        )
                }
        }
    }
}
