package printscript.v1.interpreter.internal.statement

import printscript.ast.expression.Expression
import printscript.ast.statement.VariableDeclarationStatement
import printscript.interpreter.ExecutionResult
import printscript.interpreter.SemanticError
import printscript.interpreter.StatementExecutor
import printscript.statement.Statement
import printscript.v1.interpreter.PrintScriptV1Environment
import printscript.v1.interpreter.PrintScriptV1ExpressionEvaluator
import printscript.v1.interpreter.PrintScriptV1RuntimeValue
import printscript.v1.interpreter.PrintScriptV1SemanticError
import printscript.v1.interpreter.PrintScriptV1VariableBinding
import printscript.v1.interpreter.internal.orReturn
import printscript.v1.interpreter.internal.value.verifyAccepts

internal class DeclarationExecutor(
    private val expressionEvaluator: PrintScriptV1ExpressionEvaluator,
) : StatementExecutor<PrintScriptV1Environment> {

    override fun supportsStatement(statement: Statement): Boolean {
        return statement is VariableDeclarationStatement
    }

    override fun executeStatement(
        statement: Statement,
        state: PrintScriptV1Environment,
    ): ExecutionResult<PrintScriptV1Environment> {
        if (statement !is VariableDeclarationStatement) {
            return ExecutionResult.Failure(
                SemanticError.UnsupportedStatement(span = statement.span),
            )
        }

        ensureNotAlreadyDeclared(statement, state)
            .orReturn { return it }

        val initialValue: PrintScriptV1RuntimeValue? =
            evaluateInitializer(statement, state)
                .orReturn { return it }

        return ExecutionResult.Success(
            state.withBinding(
                name = statement.identifier.value,
                binding = PrintScriptV1VariableBinding(
                    type = statement.declaredType,
                    value = initialValue,
                ),
            ),
        )
    }

    private fun ensureNotAlreadyDeclared(
        statement: VariableDeclarationStatement,
        state: PrintScriptV1Environment,
    ): ExecutionResult<Unit> {
        val name: String = statement.identifier.value

        if (state.lookupBinding(name) != null) {
            return ExecutionResult.Failure(
                PrintScriptV1SemanticError.AlreadyDeclaredVariable(
                    name = name,
                    span = statement.span,
                ),
            )
        }

        return ExecutionResult.Success(Unit)
    }

    private fun evaluateInitializer(
        statement: VariableDeclarationStatement,
        state: PrintScriptV1Environment,
    ): ExecutionResult<PrintScriptV1RuntimeValue?> {
        val initializer: Expression = statement.initializer
            ?: return ExecutionResult.Success(null)

        val value: PrintScriptV1RuntimeValue =
            expressionEvaluator.evaluateExpression(initializer, state)
                .orReturn { return it }

        statement.declaredType.verifyAccepts(
            value = value,
            variableName = statement.identifier.value,
            span = statement.span,
        ).orReturn { return it }

        return ExecutionResult.Success(value)
    }
}
