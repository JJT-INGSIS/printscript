package printscript.interpreter.environment

import printscript.interpreter.value.RuntimeValue

interface Environment {

    fun lookupBinding(name: String): VariableBinding?

    fun withBinding(name: String, binding: VariableBinding): Environment
}