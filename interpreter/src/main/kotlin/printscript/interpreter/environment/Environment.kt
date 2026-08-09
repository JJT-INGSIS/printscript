package printscript.interpreter.environment

import printscript.interpreter.value.RuntimeValue

interface Environment {
    fun lookup(name: String): VariableBinding?
    fun declare(name: String, binding: VariableBinding)
    fun update(name: String, value: RuntimeValue)
}