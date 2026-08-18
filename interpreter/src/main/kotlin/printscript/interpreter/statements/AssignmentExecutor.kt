package printscript.interpreter.statements

import printscript.ast.statement.AssignmentStatement
import printscript.ast.statement.Statement
import printscript.interpreter.ExecutionContext
import printscript.interpreter.ExecutionResult
import printscript.interpreter.SemanticError
import printscript.interpreter.environment.VariableBinding
import printscript.interpreter.orFail
import printscript.interpreter.orReturn
import printscript.interpreter.value.RuntimeValue

internal class AssignmentExecutor : StatementExecutor {

    override fun supports(
        statement: Statement,
    ): Boolean {
        return statement is AssignmentStatement
    }

    override fun execute(
        statement: Statement,
        context: ExecutionContext,
    ): ExecutionResult<Unit> {
        val assignment: AssignmentStatement =
            statementOrFail(statement, AssignmentStatement::class)
                .orReturn { return it }

        val binding: VariableBinding =
            resolveBinding(assignment, context)
                .orReturn { return it }

        val value: RuntimeValue =
            context.evaluate(assignment.expression)
                .orReturn { return it }

        ensureTypeMatches(assignment, binding, value)
            .orReturn { return it }

        return updateVariable(assignment, value, context)
    }

    private fun resolveBinding(
        assignment: AssignmentStatement,
        context: ExecutionContext,
    ): ExecutionResult<VariableBinding> {
        val name: String = assignment.target.value

        return context.environment.lookup(name)
            .orFail {
                SemanticError.UndeclaredVariable(
                    name = name,
                    span = assignment.span,
                )
            }
    }

    private fun ensureTypeMatches(
        assignment: AssignmentStatement,
        binding: VariableBinding,
        value: RuntimeValue,
    ): ExecutionResult<Unit> =
        ensureType(
            name = assignment.target.value,
            expected = binding.type,
            actual = value.type,
            span = assignment.span,
        )

    private fun updateVariable(
        assignment: AssignmentStatement,
        value: RuntimeValue,
        context: ExecutionContext,
    ): ExecutionResult<Unit> {
        context.environment.update(assignment.target.value, value)

        return ExecutionResult.Success(Unit)
    }
}