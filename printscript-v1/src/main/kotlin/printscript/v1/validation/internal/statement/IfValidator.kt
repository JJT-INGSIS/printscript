package printscript.v1.validation.internal.statement

import printscript.ast.DeclaredType
import printscript.ast.statement.BlockStatement
import printscript.ast.statement.IfStatement
import printscript.interpreter.ExecutionResult
import printscript.interpreter.StatementExecutionContext
import printscript.interpreter.StatementExecutor
import printscript.statement.Statement
import printscript.v1.interpreter.PrintScriptV1SemanticError
import printscript.v1.interpreter.internal.orReturn
import printscript.v1.interpreter.internal.statement.unsupportedStatement
import printscript.v1.validation.internal.ValidationEnvironment

internal class IfValidator : StatementExecutor<ValidationEnvironment> {

    override fun supportsStatement(statement: Statement): Boolean = statement is IfStatement

    override fun executeStatement(
        statement: Statement,
        context: StatementExecutionContext<ValidationEnvironment>,
    ): ExecutionResult<ValidationEnvironment> {
        if (statement !is IfStatement) {
            return unsupportedStatement(statement)
        }
        validateCondition(statement, context.state).orReturn { return it }
        val thenState = validateBranch(statement.thenBranch, context).orReturn { return it }
        val elseState = statement.elseBranch?.let { branch ->
            validateBranch(branch, context).orReturn { return it }
        } ?: context.state
        return ExecutionResult.Success(thenState.intersectingInitialization(elseState))
    }

    private fun validateCondition(statement: IfStatement, environment: ValidationEnvironment): ExecutionResult<Unit> {
        val condition = environment.initializedBindingOf(statement.condition).orReturn { return it }
        if (condition.type != DeclaredType.BOOLEAN) {
            return ExecutionResult.Failure(
                PrintScriptV1SemanticError.InvalidIfCondition(
                    name = statement.condition.value,
                    actual = condition.type,
                    span = statement.condition.span,
                ),
            )
        }
        return ExecutionResult.Success(Unit)
    }

    private fun validateBranch(
        branch: BlockStatement,
        context: StatementExecutionContext<ValidationEnvironment>,
    ): ExecutionResult<ValidationEnvironment> {
        val scopedContext = context.withState(context.state.enteringScope())
        val result = scopedContext.executeStatements(branch.statements).orReturn { return it }
        return ExecutionResult.Success(result.leavingScope())
    }
}
