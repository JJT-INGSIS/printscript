package printscript.interpreter.statements

import printscript.interpreter.ExecutionContext
import printscript.interpreter.ExecutionResult
import printscript.interpreter.SemanticError
import printscript.interpreter.environment.VariableBinding
import printscript.interpreter.orReturn
import printscript.interpreter.value.RuntimeValue
import printscript.model.ast.expression.Expression
import printscript.model.ast.statement.Statement
import printscript.model.ast.statement.VariableDeclarationStatement

internal class DeclarationExecutor : StatementExecutor {

    override fun supports(
        statement: Statement,
    ): Boolean {
        return statement is VariableDeclarationStatement
    }

    override fun execute(
        statement: Statement,
        context: ExecutionContext,
    ): ExecutionResult<Unit> {
        val declaration: VariableDeclarationStatement =
            statementOrFail(statement, VariableDeclarationStatement::class)
                .orReturn { return it }

        val name: String = declaration.identifier.value

        if (isAlreadyDeclared(name, context)) {
            return ExecutionResult.Failure(
                SemanticError.AlreadyDeclaredVariable(
                    name = name,
                    span = declaration.span,
                ),
            )
        }

        val initialValue: RuntimeValue? =
            evaluateInitializer(declaration, context)
                .orReturn { return it }

        val binding = VariableBinding(
            type = declaration.declaredType,
            value = initialValue,
        )

        context.environment.declare(name, binding)

        return ExecutionResult.Success(Unit)
    }

    private fun isAlreadyDeclared(
        name: String,
        context: ExecutionContext,
    ): Boolean {
        return context.environment.lookup(name) != null
    }

    private fun evaluateInitializer(
        statement: VariableDeclarationStatement,
        context: ExecutionContext,
    ): ExecutionResult<RuntimeValue?> {
        val initializer: Expression = statement.initializer
            ?: return ExecutionResult.Success(null)

        val value: RuntimeValue =
            context.evaluate(initializer)
                .orReturn { return it }

        if (value.type != statement.declaredType) {
            return ExecutionResult.Failure(
                SemanticError.TypeMismatch(
                    name = statement.identifier.value,
                    expected = statement.declaredType,
                    actual = value.type,
                    span = statement.span,
                ),
            )
        }

        return ExecutionResult.Success(value)
    }
}