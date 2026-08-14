package printscript.interpreter.statements

import printscript.interpreter.ExecutionContext
import printscript.interpreter.ExecutionResult
import printscript.interpreter.SemanticError
import printscript.interpreter.orReturn
import printscript.interpreter.value.RuntimeValue
import printscript.model.ast.statement.PrintlnStatement
import printscript.model.ast.statement.Statement

internal class PrintlnExecutor : StatementExecutor {

    override fun supports(
        statement: Statement,
    ): Boolean {
        return statement is PrintlnStatement
    }

    override fun execute(
        statement: Statement,
        context: ExecutionContext,
    ): ExecutionResult<Unit> {
        if (statement !is PrintlnStatement) {
            return ExecutionResult.Failure(
                SemanticError.UnsupportedStatement(
                    span = statement.span,
                ),
            )
        }

        val value: RuntimeValue =
            context.evaluate(statement.argument)
                .orReturn { return it }

        context.emit(value.asText())

        return ExecutionResult.Success(Unit)
    }
}