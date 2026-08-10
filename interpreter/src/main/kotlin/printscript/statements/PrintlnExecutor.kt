package printscript.interpreter.statements

import printscript.interpreter.ExecutionContext
import printscript.model.ast.statement.PrintlnStatement

class PrintlnExecutor : StatementExecutor<PrintlnStatement> {

    override fun execute(statement: PrintlnStatement, context: ExecutionContext) {
        val value = context.evaluate(statement.argument)
        context.emit(value.asText())
    }
}