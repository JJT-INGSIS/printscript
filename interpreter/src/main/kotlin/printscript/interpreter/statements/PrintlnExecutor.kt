package printscript.interpreter.statements

import printscript.ast.statement.PrintlnStatement
import printscript.ast.statement.Statement
import printscript.interpreter.ExecutionContext
import printscript.interpreter.ExecutionResult
import printscript.interpreter.SemanticError
import printscript.interpreter.environment.Environment
import printscript.interpreter.orReturn
import printscript.interpreter.value.RuntimeValue

internal class PrintlnExecutor : StatementExecutor {

    override fun supportsStatement(
        statement: Statement,
    ): Boolean {
        return statement is PrintlnStatement
    }

    override fun executeStatement(
        statement: Statement,
        context: ExecutionContext,
    ): ExecutionResult<Environment> {
        if (statement !is PrintlnStatement) {
            return ExecutionResult.Failure(
                SemanticError.UnsupportedStatement(span = statement.span),
            )
        }

        val value: RuntimeValue =
            context.evaluateExpression(statement.argument)
                .orReturn { return it }

        context.writeLine(value.asText())

        return ExecutionResult.Success(context.environment)
    }
}