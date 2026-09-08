package printscript.v1.interpreter.internal.statement

import printscript.ast.statement.AssignmentStatement
import printscript.interpreter.ExecutionResult
import printscript.interpreter.StatementExecutionContext
import printscript.interpreter.StatementExecutor
import printscript.model.source.SourceSpan
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
            return unsupportedStatement(statement)
        }

        val state: Environment = context.state
        val name: String = statement.target.value

        val binding: VariableBinding = findAssignableBinding(
            name = name,
            span = statement.span,
            state = state,
        ).orReturn { return it }

        val value: RuntimeValue = expressionEvaluator.evaluateExpression(
            expression = statement.expression,
            environment = state,
            expectedType = binding.type,
        ).orReturn { return it }

        binding.type.verifyAccepts(
            value = value,
            variableName = name,
            span = statement.span,
        ).orReturn { return it }

        return ExecutionResult.Success(
            state.reassign(
                name = name,
                value = value,
            ),
        )
    }

    private fun findAssignableBinding(
        name: String,
        span: SourceSpan,
        state: Environment,
    ): ExecutionResult<VariableBinding> {
        val binding: VariableBinding = state.findBinding(name)
            ?: return ExecutionResult.Failure(
                PrintScriptV1SemanticError.UndeclaredVariable(
                    name = name,
                    span = span,
                ),
            )

        if (!binding.reassignable) {
            return ExecutionResult.Failure(
                PrintScriptV1SemanticError.ConstantReassignment(
                    name = name,
                    span = span,
                ),
            )
        }

        return ExecutionResult.Success(binding)
    }
}
