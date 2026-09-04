package printscript.v1.interpreter.internal.statement

import printscript.ast.statement.AssignmentStatement
import printscript.interpreter.ExecutionResult
import printscript.interpreter.SemanticError
import printscript.interpreter.StatementExecutionContext
import printscript.interpreter.StatementExecutor
import printscript.runtime.Environment
import printscript.runtime.ExpressionEvaluator
import printscript.runtime.RuntimeValue
import printscript.runtime.VariableBinding
import printscript.statement.Statement
import printscript.v1.interpreter.PrintScriptV1SemanticError
import printscript.v1.interpreter.internal.orReturn
import printscript.v1.interpreter.internal.value.verifyAccepts

internal class AssignmentExecutor(
    private val expressionEvaluator: ExpressionEvaluator,
) : StatementExecutor<Environment> {

    override fun supportsStatement(statement: Statement): Boolean {
        return statement is AssignmentStatement
    }

    override fun executeStatement(
        statement: Statement,
        context: StatementExecutionContext<Environment>,
    ): ExecutionResult<Environment> {
        if (statement !is AssignmentStatement) {
            return ExecutionResult.Failure(
                SemanticError.UnsupportedStatement(span = statement.span),
            )
        }

        val state: Environment = context.state
        val name: String = statement.target.value

        val binding: VariableBinding = findAssignableBinding(statement, state)
            .orReturn { return it }

        val value: RuntimeValue =
            expressionEvaluator.evaluateExpression(statement.expression, state)
                .orReturn { return it }

        binding.type.verifyAccepts(
            value = value,
            variableName = name,
            span = statement.span,
        ).orReturn { return it }

        return ExecutionResult.Success(
            state.reassigning(
                name = name,
                value = value,
            ),
        )
    }

    private fun findAssignableBinding(
        statement: AssignmentStatement,
        state: Environment,
    ): ExecutionResult<VariableBinding> {
        val name: String = statement.target.value
        val binding: VariableBinding = state.lookupBinding(name)
            ?: return ExecutionResult.Failure(
                PrintScriptV1SemanticError.UndeclaredVariable(
                    name = name,
                    span = statement.span,
                ),
            )

        if (!binding.reassignable) {
            return ExecutionResult.Failure(
                PrintScriptV1SemanticError.ConstantReassignment(
                    name = name,
                    span = statement.span,
                ),
            )
        }

        return ExecutionResult.Success(binding)
    }
}
