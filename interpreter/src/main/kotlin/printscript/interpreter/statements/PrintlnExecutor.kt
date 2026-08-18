package printscript.interpreter.statements

import printscript.ast.statement.PrintlnStatement
import printscript.ast.statement.Statement
import printscript.interpreter.ExecutionContext
import printscript.interpreter.ExecutionResult
import printscript.interpreter.orReturn
import printscript.interpreter.value.RuntimeValue

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
        val printlnStatement: PrintlnStatement =
            statementOrFail(statement, PrintlnStatement::class)
                .orReturn { return it }

        val value: RuntimeValue =
            context.evaluate(printlnStatement.argument)
                .orReturn { return it }

        return emit(value, context)
    }

    private fun emit(
        value: RuntimeValue,
        context: ExecutionContext,
    ): ExecutionResult<Unit> {
        context.emit(value.asText())

        return ExecutionResult.Success(Unit)
    }
}