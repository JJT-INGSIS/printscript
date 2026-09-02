package printscript.v1.interpreter

import printscript.interpreter.Interpreter
import printscript.interpreter.InterpreterFactory
import printscript.interpreter.StatementExecutor
import printscript.runtime.Environment
import printscript.runtime.EnvironmentFactory
import printscript.runtime.ExpressionEvaluator
import printscript.runtime.ProgramOutput
import printscript.v1.interpreter.internal.statement.AssignmentExecutor
import printscript.v1.interpreter.internal.statement.DeclarationExecutor
import printscript.v1.interpreter.internal.statement.PrintlnExecutor

public object PrintScriptV1InterpreterFactory {

    /**
     * Creates the V1 interpreter. Additional executors are evaluated before
     * the executors included by V1, allowing callers to extend or override
     * statement behavior. Expression evaluation can be replaced independently
     * while keeping the closed V1 expression hierarchy.
     */
    public fun create(
        output: ProgramOutput,
        additionalStatementExecutors: List<StatementExecutor<Environment>> = emptyList(),
        expressionEvaluator: ExpressionEvaluator =
            PrintScriptV1ExpressionEvaluatorFactory.create(),
    ): Interpreter {
        return InterpreterFactory.create(
            initialState = EnvironmentFactory.empty(),
            statementExecutors =
            additionalStatementExecutors +
                printScriptV1StatementExecutors(
                    expressionEvaluator = expressionEvaluator,
                    output = output,
                ),
        )
    }

    private fun printScriptV1StatementExecutors(
        expressionEvaluator: ExpressionEvaluator,
        output: ProgramOutput,
    ): List<StatementExecutor<Environment>> {
        return listOf(
            DeclarationExecutor(expressionEvaluator),
            AssignmentExecutor(expressionEvaluator),
            PrintlnExecutor(
                expressionEvaluator = expressionEvaluator,
                output = output,
            ),
        )
    }
}
