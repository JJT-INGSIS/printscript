package printscript.interpreter

import printscript.interpreter.environment.MapEnvironment
import printscript.interpreter.expressions.ExpressionEvaluator
import printscript.interpreter.output.ProgramOutput
import printscript.interpreter.statements.AssignmentExecutor
import printscript.interpreter.statements.DeclarationExecutor
import printscript.interpreter.statements.PrintlnExecutor
import printscript.interpreter.statements.StatementExecutor
import printscript.interpreter.statements.StatementExecutorDispatcher

public object PrintScriptInterpreterFactory {

    public fun createV1(output: ProgramOutput): Interpreter {
        return PrintScriptInterpreter(
            output = output,
            initialEnvironment = MapEnvironment(),
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
