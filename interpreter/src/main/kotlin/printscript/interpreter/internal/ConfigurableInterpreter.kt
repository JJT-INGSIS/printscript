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

    private val statementExecutorDispatcher = StatementExecutorDispatcher(
        statementExecutors = statementExecutors,
    )
    private val initialContext: StatementExecutionContext<S> = DispatchingStatementExecutionContext(
        dispatcher = statementExecutorDispatcher,
        state = initialState,
    )

    override fun interpret(source: StatementSource): InterpretationResult {
        return interpretationSteps(source)
            .filterIsInstance<InterpretationStep.Finished>()
            .first()
            .result
    }

    private fun interpretationSteps(source: StatementSource): Sequence<InterpretationStep<S>> {
        return generateSequence<InterpretationStep<S>>(
            InterpretationStep.Pending(
                source = source,
                context = initialContext,
            ),
        ) { step ->
            advance(step)
        }
    }

    private fun advance(step: InterpretationStep<S>): InterpretationStep<S>? {
        return when (step) {
            is InterpretationStep.Finished -> null

            is InterpretationStep.Pending -> readAndExecute(step)
        }
    }

    private fun readAndExecute(step: InterpretationStep.Pending<S>): InterpretationStep<S> {
        return when (val readResult = step.source.nextStatement()) {
            StatementReadResult.EndOfInput ->
                InterpretationStep.Finished(InterpretationResult.Success)

            is StatementReadResult.Failure ->
                InterpretationStep.Finished(
                    InterpretationResult.ParseFailure(
                        error = readResult.error,
                    ),
                )

            is StatementReadResult.Success ->
                continueAfter(
                    readResult = readResult,
                    context = step.context,
                )
        }
    }

    private fun continueAfter(
        readResult: StatementReadResult.Success,
        context: StatementExecutionContext<S>,
    ): InterpretationStep<S> {
        return when (val execution = context.executeStatement(readResult.statement)) {
            is ExecutionResult.Failure -> InterpretationStep.Finished(
                InterpretationResult.SemanticFailure(
                    error = execution.error,
                ),
            )

            is ExecutionResult.Success -> InterpretationStep.Pending(
                source = readResult.remainingSource,
                context = context.withState(execution.value),
            )
        }
    }

    private sealed interface InterpretationStep<out S> {

        data class Pending<S>(
            val source: StatementSource,
            val context: StatementExecutionContext<S>,
        ) : InterpretationStep<S>

        data class Finished(
            val result: InterpretationResult,
        ) : InterpretationStep<Nothing>
    }
}
