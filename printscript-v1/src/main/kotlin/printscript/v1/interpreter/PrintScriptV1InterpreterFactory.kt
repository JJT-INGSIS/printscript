package printscript.v1.interpreter

import printscript.interpreter.Interpreter
import printscript.interpreter.InterpreterFactory
import printscript.interpreter.StatementExecutor
import printscript.v1.interpreter.internal.environment.MapEnvironment
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
        output: PrintScriptV1ProgramOutput,
        additionalStatementExecutors: List<StatementExecutor<PrintScriptV1Environment>> = emptyList(),
        expressionEvaluator: PrintScriptV1ExpressionEvaluator =
            PrintScriptV1ExpressionEvaluatorFactory.create(),
    ): Interpreter {
        return InterpreterFactory.create(
            initialState = MapEnvironment(),
            statementExecutors =
            additionalStatementExecutors +
                printScriptV1StatementExecutors(
                    expressionEvaluator = expressionEvaluator,
                    output = output,
                ),
        )
    }

    private fun printScriptV1StatementExecutors(
        expressionEvaluator: PrintScriptV1ExpressionEvaluator,
        output: PrintScriptV1ProgramOutput,
    ): List<StatementExecutor<PrintScriptV1Environment>> {
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
