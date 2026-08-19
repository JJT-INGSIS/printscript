package printscript.interpreter

import printscript.ast.expression.Expression
import printscript.interpreter.environment.Environment
import printscript.interpreter.value.RuntimeValue

interface ExecutionContext {

    val environment: Environment

    fun evaluateExpression(expression: Expression): ExecutionResult<RuntimeValue>

    fun writeLine(line: String)
}