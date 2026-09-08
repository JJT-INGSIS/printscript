package printscript.v1.validation.internal.statement

import printscript.ast.statement.PrintlnStatement
import printscript.interpreter.ExecutionResult
import printscript.interpreter.StatementExecutionContext
import printscript.interpreter.StatementExecutor
import printscript.statement.Statement
import printscript.v1.interpreter.internal.orReturn
import printscript.v1.interpreter.internal.statement.unsupportedStatement
import printscript.v1.validation.internal.ValidationEnvironment
import printscript.v1.validation.internal.expression.ExpressionTypeResolver

internal class PrintlnValidator(
    private val expressionTypes: ExpressionTypeResolver,
) : StatementExecutor<ValidationEnvironment> {

    override fun supportsStatement(statement: Statement): Boolean = statement is PrintlnStatement

    override fun executeStatement(
        statement: Statement,
        context: StatementExecutionContext<ValidationEnvironment>,
    ): ExecutionResult<ValidationEnvironment> {
        if (statement !is PrintlnStatement) {
            return unsupportedStatement(statement)
        }
        expressionTypes.typeOf(statement.argument, context.state).orReturn { return it }
        return ExecutionResult.Success(context.state)
    }
}
