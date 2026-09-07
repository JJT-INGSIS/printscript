package printscript.v1.interpreter.internal.statement

import printscript.ast.statement.BlockStatement
import printscript.ast.statement.IfStatement
import printscript.interpreter.ExecutionResult
import printscript.interpreter.SemanticError
import printscript.interpreter.StatementExecutionContext
import printscript.interpreter.StatementExecutor
import printscript.runtime.BooleanValue
import printscript.runtime.Environment
import printscript.statement.Statement
import printscript.v1.interpreter.PrintScriptV1SemanticError
import printscript.v1.interpreter.internal.orReturn
import printscript.v1.interpreter.internal.value.resolveInitializedValue

internal class IfExecutor : StatementExecutor<Environment> {

    override fun supportsStatement(statement: Statement): Boolean {
        return statement is IfStatement
    }

    override fun executeStatement(
        statement: Statement,
        context: StatementExecutionContext<Environment>,
    ): ExecutionResult<Environment> {
        if (statement !is IfStatement) {
            return ExecutionResult.Failure(
                SemanticError.UnsupportedStatement(span = statement.span),
            )
        }

        val condition: BooleanValue = evaluateCondition(statement, context.state)
            .orReturn { return it }

        val branch: BlockStatement? = if (condition.value) {
            statement.thenBranch
        } else {
            statement.elseBranch
        }

        if (branch == null) {
            return ExecutionResult.Success(context.state)
        }

        return executeBranch(branch, context)
    }

    private fun evaluateCondition(statement: IfStatement, environment: Environment): ExecutionResult<BooleanValue> {
        val name: String = statement.condition.value
        val value = environment.resolveInitializedValue(name, statement.condition.span).orReturn { return it }

        if (value !is BooleanValue) {
            return ExecutionResult.Failure(
                PrintScriptV1SemanticError.InvalidIfCondition(
                    name = name,
                    actual = value.type,
                    span = statement.condition.span,
                ),
            )
        }

        return ExecutionResult.Success(value)
    }

    private fun executeBranch(
        branch: BlockStatement,
        context: StatementExecutionContext<Environment>,
    ): ExecutionResult<Environment> {
        val scopedContext: StatementExecutionContext<Environment> = context.withState(
            context.state.enterScope(),
        )

        return when (val result = scopedContext.executeStatements(branch.statements)) {
            is ExecutionResult.Failure -> result
            is ExecutionResult.Success -> ExecutionResult.Success(result.value.leaveScope())
        }
    }
}
