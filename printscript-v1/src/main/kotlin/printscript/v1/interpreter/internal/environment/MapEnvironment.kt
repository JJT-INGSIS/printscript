package printscript.v1.interpreter.internal.environment

import printscript.v1.interpreter.PrintScriptV1Environment
import printscript.v1.interpreter.PrintScriptV1VariableBinding

internal class MapEnvironment private constructor(
    private val bindings: Map<String, PrintScriptV1VariableBinding>,
) : PrintScriptV1Environment {

    constructor() : this(emptyMap())

    override fun lookupBinding(name: String): PrintScriptV1VariableBinding? {
        return bindings[name]
    }

    override fun withBinding(name: String, binding: PrintScriptV1VariableBinding): PrintScriptV1Environment {
        return MapEnvironment(bindings + (name to binding))
    }
}
