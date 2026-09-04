package printscript.runtime.internal.environment

import printscript.runtime.Environment
import printscript.runtime.RuntimeValue
import printscript.runtime.VariableBinding

internal class MapEnvironment private constructor(
    private val scopes: List<Map<String, VariableBinding>>,
) : Environment {

    constructor() : this(listOf(emptyMap()))

    override fun lookupBinding(name: String): VariableBinding? {
        return scopes
            .asReversed()
            .firstNotNullOfOrNull { scope -> scope[name] }
    }

    override fun declaring(name: String, binding: VariableBinding): Environment {
        return MapEnvironment(
            scopes = scopes.dropLast(1) + (scopes.last() + (name to binding)),
        )
    }

    override fun reassigning(name: String, value: RuntimeValue): Environment {
        val declaringScopeIndex = scopes.indexOfLast { scope -> name in scope }

        require(declaringScopeIndex >= 0)

        return MapEnvironment(
            scopes = scopes.mapIndexed { index, scope ->
                if (index == declaringScopeIndex) {
                    scope + (name to scope.getValue(name).copy(value = value))
                } else {
                    scope
                }
            },
        )
    }

    override fun enteringScope(): Environment {
        return MapEnvironment(scopes + emptyMap())
    }

    override fun leavingScope(): Environment {
        require(scopes.size > GLOBAL_SCOPE_COUNT)

        return MapEnvironment(scopes.dropLast(1))
    }

    private companion object {
        const val GLOBAL_SCOPE_COUNT = 1
    }
}
