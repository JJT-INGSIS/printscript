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

        ensureNotAlreadyDeclared(declaration, context)
            .orReturn { return it }

        val initialValue: RuntimeValue? =
            evaluateInitializer(declaration, context)
                .orReturn { return it }

        return declare(declaration, initialValue, context)
    }

    private fun ensureNotAlreadyDeclared(
        declaration: VariableDeclarationStatement,
        context: ExecutionContext,
    ): ExecutionResult<Unit> {
        val name: String = declaration.identifier.value

        if (context.environment.lookup(name) != null) {
            return ExecutionResult.Failure(
                SemanticError.AlreadyDeclaredVariable(
                    name = name,
                    span = declaration.span,
                ),
            )
        }

        return ExecutionResult.Success(Unit)
    }

    private fun declare(
        declaration: VariableDeclarationStatement,
        value: RuntimeValue?,
        context: ExecutionContext,
    ): ExecutionResult<Unit> {
        val binding = VariableBinding(
            type = declaration.declaredType,
            value = value,
        )

        context.environment.declare(declaration.identifier.value, binding)

        return ExecutionResult.Success(Unit)
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

        ensureType(
            name = statement.identifier.value,
            expected = statement.declaredType,
            actual = value.type,
            span = statement.span,
        ).orReturn { return it }

        return ExecutionResult.Success(value)
    }
}