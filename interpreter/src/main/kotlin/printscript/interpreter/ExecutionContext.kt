package printscript.interpreter

import printscript.interpreter.environment.Environment
import printscript.interpreter.value.RuntimeValue
import printscript.ast.expression.Expression

interface ExecutionContext {

    val environment: Environment

    fun evaluate(expression: printscript.ast.expression.Expression): ExecutionResult<RuntimeValue>

    fun emit(line: String)
}