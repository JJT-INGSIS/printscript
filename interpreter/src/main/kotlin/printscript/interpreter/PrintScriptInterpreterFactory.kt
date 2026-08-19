package printscript.interpreter

import printscript.interpreter.environment.Environment
import printscript.interpreter.environment.MapEnvironment
import printscript.interpreter.expressions.ExpressionEvaluator
import printscript.interpreter.output.ProgramOutput
import printscript.interpreter.statements.AssignmentExecutor
import printscript.interpreter.statements.DeclarationExecutor
import printscript.interpreter.statements.PrintlnExecutor
import printscript.interpreter.statements.StatementExecutor
import printscript.interpreter.statements.StatementExecutorDispatcher

object PrintScriptInterpreterFactory {

    fun createV1(
        output: ProgramOutput,
        environment: Environment = MapEnvironment(),
    ): Interpreter {
        return PrintScriptInterpreter(
            output = output,
            initialEnvironment = environment,
            expressionEvaluator = ExpressionEvaluator(),
            statementExecutorDispatcher = StatementExecutorDispatcher(
                executors = v1StatementExecutors(),
            ),
        )
    }

    private fun v1StatementExecutors(): List<StatementExecutor> {
        return listOf(
            DeclarationExecutor(),
            AssignmentExecutor(),
            PrintlnExecutor(),
        )
    }
}