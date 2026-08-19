package printscript.interpreter.statements

import printscript.ast.expression.Expression
import printscript.ast.statement.Statement
import printscript.ast.statement.VariableDeclarationStatement
import printscript.interpreter.ExecutionContext
import printscript.interpreter.ExecutionResult
import printscript.interpreter.SemanticError
import printscript.interpreter.environment.Environment
import printscript.interpreter.environment.VariableBinding
import printscript.interpreter.orReturn
import printscript.interpreter.value.RuntimeValue
import printscript.interpreter.value.verifyAccepts

internal class DeclarationExecutor : StatementExecutor {

    override fun supportsStatement(
        statement: Statement,
    ): Boolean {
        return statement is VariableDeclarationStatement
    }

    override fun executeStatement(
        statement: Statement,
        context: ExecutionContext,
    ): ExecutionResult<Environment> {
        if (statement !is VariableDeclarationStatement) {
            return ExecutionResult.Failure(
                SemanticError.UnsupportedStatement(span = statement.span),
            )
        }

        ensureNotAlreadyDeclared(statement, context)
            .orReturn { return it }

        val initialValue: RuntimeValue? =
            evaluateInitializer(statement, context)
                .orReturn { return it }

        return ExecutionResult.Success(
            context.environment.withBinding(
                name = statement.identifier.value,
                binding = VariableBinding(
                    type = statement.declaredType,
                    value = initialValue,
                ),
            ),
        )
    }

    private fun ensureNotAlreadyDeclared(
        statement: VariableDeclarationStatement,
        context: ExecutionContext,
    ): ExecutionResult<Unit> {
        val name: String = statement.identifier.value

        if (context.environment.lookupBinding(name) != null) {
            return ExecutionResult.Failure(
                SemanticError.AlreadyDeclaredVariable(
                    name = name,
                    span = statement.span,
                ),
            )
        }

        return ExecutionResult.Success(Unit)
    }

    private fun evaluateInitializer(
        statement: VariableDeclarationStatement,
        context: ExecutionContext,
    ): ExecutionResult<RuntimeValue?> {
        val initializer: Expression = statement.initializer
            ?: return ExecutionResult.Success(null)

        val value: RuntimeValue =
            context.evaluateExpression(initializer)
                .orReturn { return it }

        statement.declaredType.verifyAccepts(
            value = value,
            variableName = statement.identifier.value,
            span = statement.span,
        ).orReturn { return it }

        return ExecutionResult.Success(value)
    }
}