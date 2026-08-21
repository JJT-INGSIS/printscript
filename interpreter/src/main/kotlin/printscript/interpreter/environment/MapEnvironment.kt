package printscript.interpreter.environment

internal class MapEnvironment private constructor(
    private val bindings: Map<String, VariableBinding>,
) : Environment {

    constructor() : this(emptyMap())

    override fun lookupBinding(name: String): VariableBinding? {
        return bindings[name]
    }

    override fun withBinding(name: String, binding: VariableBinding): Environment {
        return MapEnvironment(bindings + (name to binding))
    }
}
