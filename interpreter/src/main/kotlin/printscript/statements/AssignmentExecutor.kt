package printscript.interpreter.statements

import printscript.interpreter.ExecutionContext
import printscript.interpreter.InterpreterException
import printscript.interpreter.displayNameOf
import printscript.model.ast.statement.AssignmentStatement

class AssignmentExecutor : StatementExecutor<AssignmentStatement> {

    override fun execute(statement: AssignmentStatement, context: ExecutionContext) {
        val name = statement.target.value

        val binding = context.environment.lookup(name)
        if (binding == null) {
            throw InterpreterException("La variable '$name' no está declarada", statement.span)
        }

        val value = context.evaluate(statement.expression)

        if (value.type != binding.type) {
            throw InterpreterException(
                "No se puede asignar un valor de tipo ${displayNameOf(value.type)} " +
                        "a la variable '$name' de tipo ${displayNameOf(binding.type)}",
                statement.span
            )
        }

        context.environment.update(name, value)
    }
}