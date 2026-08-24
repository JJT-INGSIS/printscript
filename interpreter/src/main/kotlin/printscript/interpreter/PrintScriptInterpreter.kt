package printscript.interpreter

import printscript.ast.statement.Statement
import printscript.interpreter.environment.Environment
import printscript.interpreter.expressions.ExpressionEvaluator
import printscript.interpreter.output.ProgramOutput
import printscript.interpreter.statements.StatementExecutorDispatcher
import printscript.statement.StatementReadResult
import printscript.statement.StatementSource

internal class PrintScriptInterpreter(
    private val output: ProgramOutput,
    private val initialEnvironment: Environment,
    private val expressionEvaluator: ExpressionEvaluator,
    private val statementExecutorDispatcher: StatementExecutorDispatcher,
) : Interpreter {

    override fun interpret(source: StatementSource): InterpretationResult {
        return interpretationSteps(source)
            .filterIsInstance<InterpretationStep.Finished>()
            .first()
            .result
    }

    /**
     * Cada paso lleva la fuente y el entorno con los que sigue el
     * siguiente. La secuencia es perezosa, así que un programa largo no
     * consume ni memoria ni stack de más.
     */
    private fun interpretationSteps(source: StatementSource): Sequence<InterpretationStep> {
        return generateSequence<InterpretationStep>(
            InterpretationStep.Pending(
                source = source,
                environment = initialEnvironment,
            ),
        ) { step ->
            advance(step)
        }
    }

    private fun advance(step: InterpretationStep): InterpretationStep? {
        return when (step) {
            is InterpretationStep.Finished -> null

            is InterpretationStep.Pending -> readAndExecute(step)
        }
    }

    private fun readAndExecute(step: InterpretationStep.Pending): InterpretationStep {
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
                    environment = step.environment,
                )
        }
    }

    private fun continueAfter(readResult: StatementReadResult.Success, environment: Environment): InterpretationStep {
        return when (
            val execution = executeStatement(
                statement = readResult.statement,
                environment = environment,
            )
        ) {
            is ExecutionResult.Failure -> InterpretationStep.Finished(
                InterpretationResult.SemanticFailure(
                    error = execution.error,
                ),
            )

            is ExecutionResult.Success -> InterpretationStep.Pending(
                source = readResult.remainingSource,
                environment = execution.value,
            )
        }
    }

    private fun executeStatement(statement: Statement, environment: Environment): ExecutionResult<Environment> {
        return statementExecutorDispatcher.dispatchToExecutor(
            statement = statement,
            context = InterpreterExecutionContext(
                environment = environment,
                expressionEvaluator = expressionEvaluator,
                output = output,
            ),
        )
    }

    private sealed interface InterpretationStep {

        data class Pending(
            val source: StatementSource,
            val environment: Environment,
        ) : InterpretationStep

        data class Finished(
            val result: InterpretationResult,
        ) : InterpretationStep
    }
}
