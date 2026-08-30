package printscript.v1.interpreter.internal.statement

import printscript.ast.statement.PrintlnStatement
import printscript.interpreter.ExecutionResult
import printscript.interpreter.SemanticError
import printscript.interpreter.StatementExecutor
import printscript.statement.Statement
import printscript.v1.interpreter.PrintScriptV1Environment
import printscript.v1.interpreter.PrintScriptV1ExpressionEvaluator
import printscript.v1.interpreter.PrintScriptV1ProgramOutput
import printscript.v1.interpreter.PrintScriptV1RuntimeValue
import printscript.v1.interpreter.internal.orReturn

internal class PrintlnExecutor(
    private val expressionEvaluator: PrintScriptV1ExpressionEvaluator,
    private val output: PrintScriptV1ProgramOutput,
) : StatementExecutor<PrintScriptV1Environment> {

    override fun supportsStatement(statement: Statement): Boolean {
        return statement is PrintlnStatement
    }

    override fun executeStatement(
        statement: Statement,
        state: PrintScriptV1Environment,
    ): ExecutionResult<PrintScriptV1Environment> {
        if (statement !is PrintlnStatement) {
            return ExecutionResult.Failure(
                SemanticError.UnsupportedStatement(span = statement.span),
            )
        }

        val value: PrintScriptV1RuntimeValue =
            expressionEvaluator.evaluateExpression(statement.argument, state)
                .orReturn { return it }

        output.writeLine(value.asText())

        return ExecutionResult.Success(state)
    }
}
