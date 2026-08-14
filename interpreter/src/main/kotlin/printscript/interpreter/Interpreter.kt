package printscript.interpreter

import printscript.interpreter.environment.Environment
import printscript.interpreter.environment.MapEnvironment
import printscript.interpreter.expressions.ExpressionEvaluator
import printscript.interpreter.output.ProgramOutput
import printscript.interpreter.statements.AssignmentExecutor
import printscript.interpreter.statements.DeclarationExecutor
import printscript.interpreter.statements.PrintlnExecutor
import printscript.interpreter.statements.StatementExecutorDispatcher
import printscript.interpreter.value.RuntimeValue
import printscript.model.ast.expression.Expression
import printscript.statement.StatementReadResult
import printscript.statement.StatementSource

class Interpreter(
    private val output: ProgramOutput,
    override val environment: Environment = MapEnvironment(),
) : ExecutionContext {

    private val evaluator = ExpressionEvaluator(environment)

    private val executorDispatcher =
        StatementExecutorDispatcher(
            executors = listOf(
                DeclarationExecutor(),
                AssignmentExecutor(),
                PrintlnExecutor(),
            ),
        )

    fun interpret(
        source: StatementSource,
    ): InterpretationResult {
        while (true) {
            when (val readResult = source.nextStatement()) {
                StatementReadResult.EndOfInput -> {
                    return InterpretationResult.Success
                }

                is StatementReadResult.Failure -> {
                    return InterpretationResult.ParseFailure(
                        readResult.error,
                    )
                }

                is StatementReadResult.Success -> {
                    when (
                        val executionResult =
                            executorDispatcher.execute(
                                statement = readResult.statement,
                                context = this,
                            )
                    ) {
                        is ExecutionResult.Success -> {
                            // Continúa con el próximo statement.
                        }

                        is ExecutionResult.Failure -> {
                            return InterpretationResult.SemanticFailure(
                                executionResult.error,
                            )
                        }
                    }
                }
            }
        }
    }

    override fun evaluate(
        expression: Expression,
    ): ExecutionResult<RuntimeValue> {
        return evaluator.evaluate(expression)
    }

    override fun emit(
        line: String,
    ) {
        output.emit(line)
    }
}