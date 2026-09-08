package printscript.v1.validation.internal.statement

import printscript.ast.statement.AssignmentStatement
import printscript.interpreter.ExecutionResult
import printscript.interpreter.StatementExecutionContext
import printscript.interpreter.StatementExecutor
import printscript.statement.Statement
import printscript.v1.interpreter.PrintScriptV1SemanticError
import printscript.v1.interpreter.internal.orReturn
import printscript.v1.interpreter.internal.statement.unsupportedStatement
import printscript.v1.interpreter.internal.value.verifyAccepts
import printscript.v1.validation.internal.ValidationEnvironment
import printscript.v1.validation.internal.expression.ExpressionTypeResolver

internal class AssignmentValidator(
    private val expressionTypes: ExpressionTypeResolver,
) : StatementExecutor<ValidationEnvironment> {

    override fun supportsStatement(statement: Statement): Boolean = statement is AssignmentStatement

    override fun executeStatement(
        statement: Statement,
        context: StatementExecutionContext<ValidationEnvironment>,
    ): ExecutionResult<ValidationEnvironment> {
        if (statement !is AssignmentStatement) {
            return unsupportedStatement(statement)
        }
        return validateAssignment(statement, context.state)
    }

    private fun validateAssignment(
        statement: AssignmentStatement,
        environment: ValidationEnvironment,
    ): ExecutionResult<ValidationEnvironment> {
        val binding = environment.bindingOf(statement.target).orReturn { return it }
        if (!binding.reassignable) {
            return ExecutionResult.Failure(
                PrintScriptV1SemanticError.ConstantReassignment(statement.target.value, statement.span),
            )
        }
        val type = expressionTypes.typeOf(statement.expression, environment, binding.type).orReturn { return it }
        binding.type.verifyAccepts(type, statement.target.value, statement.span).orReturn { return it }
        return ExecutionResult.Success(environment.initializing(statement.target.value))
    }
}
