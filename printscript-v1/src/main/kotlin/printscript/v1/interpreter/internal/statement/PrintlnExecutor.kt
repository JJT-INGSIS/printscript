package printscript.v1.interpreter.internal.statement

import printscript.ast.statement.PrintlnStatement
import printscript.interpreter.ExecutionResult
import printscript.interpreter.StatementExecutionContext
import printscript.interpreter.StatementExecutor
import printscript.runtime.Environment
import printscript.runtime.ExpressionEvaluator
import printscript.runtime.ProgramOutput
import printscript.runtime.RuntimeValue
import printscript.statement.Statement
import printscript.v1.interpreter.internal.orReturn

internal class PrintlnExecutor(
    private val expressionEvaluator: ExpressionEvaluator,
    private val output: ProgramOutput,
) : StatementExecutor<Environment> {

    override fun supportsStatement(statement: Statement): Boolean {
        return statement is PrintlnStatement
    }

    override fun executeStatement(
        statement: Statement,
        context: StatementExecutionContext<Environment>,
    ): ExecutionResult<Environment> {
        if (statement !is PrintlnStatement) {
            return unsupportedStatement(statement)
        }

        val state: Environment = context.state

        val value: RuntimeValue = expressionEvaluator.evaluateExpression(
            expression = statement.argument,
            environment = state,
            expectedType = null,
        ).orReturn { return it }

        output.writeLine(value.asText())

        return ExecutionResult.Success(state)
    }
}
