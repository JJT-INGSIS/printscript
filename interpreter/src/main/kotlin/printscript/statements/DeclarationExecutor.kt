package printscript.interpreter.statements

import printscript.interpreter.ExecutionContext
import printscript.interpreter.InterpreterException
import printscript.interpreter.displayNameOf
import printscript.interpreter.environment.VariableBinding
import printscript.interpreter.value.RuntimeValue
import printscript.model.ast.statement.VariableDeclarationStatement

class DeclarationExecutor : StatementExecutor<VariableDeclarationStatement> {

    override fun execute(statement: VariableDeclarationStatement, context: ExecutionContext) {
        val name = statement.identifier.value

        if (context.environment.lookup(name) != null) {
            throw InterpreterException("La variable '$name' ya fue declarada", statement.span)
        }

        var value: RuntimeValue? = null
        val initializer = statement.initializer

        if (initializer != null) {
            value = context.evaluate(initializer)

            if (value.type != statement.declaredType) {
                throw InterpreterException(
                    "No se puede asignar un valor de tipo ${displayNameOf(value.type)} " +
                            "a la variable '$name' de tipo ${displayNameOf(statement.declaredType)}",
                    statement.span
                )
            }
        }

        context.environment.declare(name, VariableBinding(statement.declaredType, value))
    }
}