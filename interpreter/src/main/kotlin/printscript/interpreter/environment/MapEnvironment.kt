package printscript.interpreter.environment

import printscript.interpreter.value.RuntimeValue

class MapEnvironment : Environment {

    private val bindings = mutableMapOf<String, VariableBinding>() // hacerlo inmutable, o chequweear q no mutee en runtime

    override fun lookup(name: String): VariableBinding? {
        return bindings[name]
    }

    override fun declare(name: String, binding: VariableBinding) {
        bindings[name] = binding
    }

    override fun update(name: String, value: RuntimeValue) {
        val current = bindings[name] ?: return
        bindings[name] = VariableBinding(current.type, value)
    }
}