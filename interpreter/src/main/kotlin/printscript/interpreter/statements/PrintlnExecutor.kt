package printscript.interpreter.statements

import printscript.interpreter.ExecutionContext
import printscript.interpreter.ExecutionResult
import printscript.interpreter.orReturn
import printscript.interpreter.value.RuntimeValue
import printscript.model.ast.statement.PrintlnStatement

internal class PrintlnExecutor : StatementExecutor<PrintlnStatement> {

    override fun execute(
        statement: PrintlnStatement,
        context: ExecutionContext,
    ): ExecutionResult<Unit> {
        val value: RuntimeValue = context.evaluate(statement.argument).orReturn { return it }

        context.emit(value.asText())

        return ExecutionResult.Success(Unit)
    }
}