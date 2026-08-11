package printscript.interpreter.statements

import printscript.interpreter.ExecutionContext
import printscript.interpreter.ExecutionResult
import printscript.interpreter.SemanticError
import printscript.interpreter.environment.VariableBinding
import printscript.interpreter.orReturn
import printscript.interpreter.value.RuntimeValue
import printscript.model.ast.statement.AssignmentStatement

internal class AssignmentExecutor : StatementExecutor<AssignmentStatement> {

    override fun execute(
        statement: AssignmentStatement,
        context: ExecutionContext,
    ): ExecutionResult<Unit> {
        val name: String = statement.target.value

        val binding: VariableBinding? = context.environment.lookup(name)
        if (binding == null) {
            return ExecutionResult.Failure(
                SemanticError.UndeclaredVariable(
                    name = name,
                    span = statement.span,
                ),
            )
        }

        val value: RuntimeValue = context.evaluate(statement.expression).orReturn { return it }

        if (value.type != binding.type) {
            return ExecutionResult.Failure(
                SemanticError.TypeMismatch(
                    name = name,
                    expected = binding.type,
                    actual = value.type,
                    span = statement.span,
                ),
            )
        }

        context.environment.update(name, value)

        return ExecutionResult.Success(Unit)
    }
}