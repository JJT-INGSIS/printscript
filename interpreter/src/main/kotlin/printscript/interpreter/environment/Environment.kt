package printscript.interpreter.environment

interface Environment {

    fun lookupBinding(name: String): VariableBinding?

    fun withBinding(name: String, binding: VariableBinding): Environment
}