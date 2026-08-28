package printscript.interpreter.internal

import printscript.interpreter.ExecutionResult
import printscript.interpreter.InterpretationResult
import printscript.interpreter.Interpreter
import printscript.interpreter.StatementExecutor
import printscript.statement.StatementReadResult
import printscript.statement.StatementSource

internal class ConfigurableInterpreter<S>(
    private val initialState: S,
    statementExecutors: List<StatementExecutor<S>>,
) : Interpreter {

    private val statementExecutorDispatcher = StatementExecutorDispatcher(
        statementExecutors = statementExecutors,
    )

    override fun interpret(source: StatementSource): InterpretationResult {
        return interpretationSteps(source)
            .filterIsInstance<InterpretationStep.Finished>()
            .first()
            .result
    }

    /**
     * Cada paso lleva la fuente y el estado con los que sigue el
     * siguiente. La secuencia es perezosa, así que un programa largo no
     * consume ni memoria ni stack de más.
     */
    private fun interpretationSteps(source: StatementSource): Sequence<InterpretationStep<S>> {
        return generateSequence<InterpretationStep<S>>(
            InterpretationStep.Pending(
                source = source,
                state = initialState,
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
                    state = step.state,
                )
        }
    }

    private fun continueAfter(readResult: StatementReadResult.Success, state: S): InterpretationStep<S> {
        return when (
            val execution = statementExecutorDispatcher.dispatchToExecutor(
                statement = readResult.statement,
                state = state,
            )
        ) {
            is ExecutionResult.Failure -> InterpretationStep.Finished(
                InterpretationResult.SemanticFailure(
                    error = execution.error,
                ),
            )

            is ExecutionResult.Success -> InterpretationStep.Pending(
                source = readResult.remainingSource,
                state = execution.value,
            )
        }
    }

    private sealed interface InterpretationStep<out S> {

        data class Pending<S>(
            val source: StatementSource,
            val state: S,
        ) : InterpretationStep<S>

        data class Finished(
            val result: InterpretationResult,
        ) : InterpretationStep<Nothing>
    }
}
