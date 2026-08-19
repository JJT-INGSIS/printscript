package printscript.interpreter

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

    override fun interpret(
        source: StatementSource,
    ): InterpretationResult {
        var environment: Environment = initialEnvironment

        while (true) {
            when (val readResult = source.nextStatement()) {
                StatementReadResult.EndOfInput ->
                    return InterpretationResult.Success

                is StatementReadResult.Failure ->
                    return InterpretationResult.ParseFailure(
                        error = readResult.error,
                    )

                is StatementReadResult.Success -> {
                    val executionResult = executeStatement(
                        statement = readResult.statement,
                        environment = environment,
                    )

                    when (executionResult) {
                        is ExecutionResult.Failure ->
                            return InterpretationResult.SemanticFailure(
                                error = executionResult.error,
                            )

                        is ExecutionResult.Success ->
                            environment = executionResult.value
                    }
                }
            }
        }
    }

    private fun executeStatement(
        statement: printscript.ast.statement.Statement,
        environment: Environment,
    ): ExecutionResult<Environment> {
        return statementExecutorDispatcher.dispatchToExecutor(
            statement = statement,
            context = InterpreterExecutionContext(
                environment = environment,
                expressionEvaluator = expressionEvaluator,
                output = output,
            ),
        )
    }
}