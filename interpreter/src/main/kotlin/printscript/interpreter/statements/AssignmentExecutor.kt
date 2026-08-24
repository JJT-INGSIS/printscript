package printscript.interpreter.statements

import printscript.ast.statement.AssignmentStatement
import printscript.ast.statement.Statement
import printscript.interpreter.ExecutionContext
import printscript.interpreter.ExecutionResult
import printscript.interpreter.SemanticError
import printscript.interpreter.environment.Environment
import printscript.interpreter.environment.VariableBinding
import printscript.interpreter.orReturn
import printscript.interpreter.value.RuntimeValue
import printscript.interpreter.value.verifyAccepts

internal class AssignmentExecutor : StatementExecutor {

    override fun supportsStatement(statement: Statement): Boolean {
        return statement is AssignmentStatement
    }

    override fun executeStatement(statement: Statement, context: ExecutionContext): ExecutionResult<Environment> {
        if (statement !is AssignmentStatement) {
            return ExecutionResult.Failure(
                SemanticError.UnsupportedStatement(span = statement.span),
            )
        }

        val name: String = statement.target.value

        val binding: VariableBinding = context.environment.lookupBinding(name)
            ?: return ExecutionResult.Failure(
                SemanticError.UndeclaredVariable(
                    name = name,
                    span = statement.span,
                ),
            )

        val value: RuntimeValue =
            context.evaluateExpression(statement.expression)
                .orReturn { return it }

        binding.type.verifyAccepts(
            value = value,
            variableName = name,
            span = statement.span,
        ).orReturn { return it }

        return ExecutionResult.Success(
            context.environment.withBinding(
                name = name,
                binding = VariableBinding(
                    type = binding.type,
                    value = value,
                ),
            ),
        )
    }
}
