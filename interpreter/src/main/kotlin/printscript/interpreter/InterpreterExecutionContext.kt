package printscript.interpreter

import printscript.ast.expression.Expression
import printscript.interpreter.environment.Environment
import printscript.interpreter.expressions.ExpressionEvaluator
import printscript.interpreter.output.ProgramOutput
import printscript.interpreter.value.RuntimeValue

internal class InterpreterExecutionContext(
    override val environment: Environment,
    private val expressionEvaluator: ExpressionEvaluator,
    private val output: ProgramOutput,
) : ExecutionContext {

    override fun evaluateExpression(expression: Expression): ExecutionResult<RuntimeValue> {
        return expressionEvaluator.evaluateExpression(
            expression = expression,
            environment = environment,
        )
    }

    override fun writeLine(line: String) {
        output.writeLine(line)
    }
}
