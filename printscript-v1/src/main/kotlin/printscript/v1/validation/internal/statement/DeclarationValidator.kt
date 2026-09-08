package printscript.v1.validation.internal.statement

import printscript.ast.DeclarationKind
import printscript.ast.statement.VariableDeclarationStatement
import printscript.interpreter.ExecutionResult
import printscript.interpreter.StatementExecutionContext
import printscript.interpreter.StatementExecutor
import printscript.statement.Statement
import printscript.v1.interpreter.PrintScriptV1SemanticError
import printscript.v1.interpreter.internal.orReturn
import printscript.v1.interpreter.internal.statement.unsupportedStatement
import printscript.v1.interpreter.internal.value.verifyAccepts
import printscript.v1.validation.internal.ValidationBinding
import printscript.v1.validation.internal.ValidationEnvironment
import printscript.v1.validation.internal.expression.ExpressionTypeResolver

internal class DeclarationValidator(
    private val expressionTypes: ExpressionTypeResolver,
) : StatementExecutor<ValidationEnvironment> {

    override fun supportsStatement(statement: Statement): Boolean = statement is VariableDeclarationStatement

    override fun executeStatement(
        statement: Statement,
        context: StatementExecutionContext<ValidationEnvironment>,
    ): ExecutionResult<ValidationEnvironment> {
        if (statement !is VariableDeclarationStatement) {
            return unsupportedStatement(statement)
        }
        if (!expressionTypes.supports(statement.declaredType)) {
            return unsupportedStatement(statement)
        }
        return validateDeclaration(statement, context.state)
    }

    private fun validateDeclaration(
        statement: VariableDeclarationStatement,
        environment: ValidationEnvironment,
    ): ExecutionResult<ValidationEnvironment> {
        val name = statement.identifier.value
        if (environment.containsInCurrentScope(name)) {
            return ExecutionResult.Failure(PrintScriptV1SemanticError.AlreadyDeclaredVariable(name, statement.span))
        }
        statement.initializer?.let { initializer ->
            val type = expressionTypes.typeOf(initializer, environment, statement.declaredType).orReturn { return it }
            statement.declaredType.verifyAccepts(type, name, statement.span).orReturn { return it }
        }
        return ExecutionResult.Success(
            environment.declaring(
                name,
                ValidationBinding(
                    type = statement.declaredType,
                    reassignable = statement.declarationKind == DeclarationKind.VARIABLE,
                    initialized = statement.initializer != null,
                ),
            ),
        )
    }
}
