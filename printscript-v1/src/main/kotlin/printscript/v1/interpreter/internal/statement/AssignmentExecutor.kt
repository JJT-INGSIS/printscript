package printscript.v1.interpreter.internal.statement

import printscript.ast.statement.AssignmentStatement
import printscript.interpreter.ExecutionResult
import printscript.interpreter.SemanticError
import printscript.interpreter.StatementExecutionContext
import printscript.interpreter.StatementExecutor
import printscript.statement.Statement
import printscript.v1.interpreter.PrintScriptV1Environment
import printscript.v1.interpreter.PrintScriptV1ExpressionEvaluator
import printscript.v1.interpreter.PrintScriptV1RuntimeValue
import printscript.v1.interpreter.PrintScriptV1SemanticError
import printscript.v1.interpreter.PrintScriptV1VariableBinding
import printscript.v1.interpreter.internal.orReturn
import printscript.v1.interpreter.internal.value.verifyAccepts

internal class AssignmentExecutor(
    private val expressionEvaluator: PrintScriptV1ExpressionEvaluator,
) : StatementExecutor<PrintScriptV1Environment> {

    override fun supportsStatement(statement: Statement): Boolean {
        return statement is AssignmentStatement
    }

    override fun executeStatement(
        statement: Statement,
        context: StatementExecutionContext<PrintScriptV1Environment>,
    ): ExecutionResult<PrintScriptV1Environment> {
        if (statement !is AssignmentStatement) {
            return ExecutionResult.Failure(
                SemanticError.UnsupportedStatement(span = statement.span),
            )
        }

        val state: PrintScriptV1Environment = context.state
        val name: String = statement.target.value

        val binding: PrintScriptV1VariableBinding = state.lookupBinding(name)
            ?: return ExecutionResult.Failure(
                PrintScriptV1SemanticError.UndeclaredVariable(
                    name = name,
                    span = statement.span,
                ),
            )

        val value: PrintScriptV1RuntimeValue =
            expressionEvaluator.evaluateExpression(statement.expression, state)
                .orReturn { return it }

        binding.type.verifyAccepts(
            value = value,
            variableName = name,
            span = statement.span,
        ).orReturn { return it }

        return ExecutionResult.Success(
            state.withBinding(
                name = name,
                binding = PrintScriptV1VariableBinding(
                    type = binding.type,
                    value = value,
                ),
            ),
        )
    }
}
