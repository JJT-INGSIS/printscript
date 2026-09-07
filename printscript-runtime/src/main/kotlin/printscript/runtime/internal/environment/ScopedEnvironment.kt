package printscript.runtime.internal.environment

import printscript.runtime.Environment
import printscript.runtime.RuntimeValue
import printscript.runtime.VariableBinding

internal class ScopedEnvironment private constructor(
    private val scopes: List<Map<String, VariableBinding>>,
) : Environment {

    constructor() : this(listOf(emptyMap()))

    override fun findBindingInCurrentScope(name: String): VariableBinding? {
        return currentScope()[name]
    }

    override fun findBinding(name: String): VariableBinding? {
        return scopes
            .asReversed()
            .firstNotNullOfOrNull { scope -> scope[name] }
    }

    override fun declare(name: String, binding: VariableBinding): Environment {
        val updatedCurrentScope = currentScope() + (name to binding)

        return ScopedEnvironment(
            scopes = scopes.dropLast(1) + updatedCurrentScope,
        )
    }

    override fun reassign(name: String, value: RuntimeValue): Environment {
        val scopeIndex = scopes.indexOfLast { scope -> name in scope }

        require(scopeIndex >= 0)

        val updatedScopes = scopes.mapIndexed { index, scope ->
            if (index == scopeIndex) {
                scope.withUpdatedValue(name, value)
            } else {
                scope
            }
        }

        return ScopedEnvironment(updatedScopes)
    }

    override fun enterScope(): Environment {
        return ScopedEnvironment(scopes + emptyMap())
    }

    override fun leaveScope(): Environment {
        require(scopes.size > GLOBAL_SCOPE_COUNT)

        return ScopedEnvironment(scopes.dropLast(1))
    }

    private fun currentScope(): Map<String, VariableBinding> {
        return scopes.last()
    }

    private fun Map<String, VariableBinding>.withUpdatedValue(
        name: String,
        value: RuntimeValue,
    ): Map<String, VariableBinding> {
        val updatedBinding = getValue(name).copy(value = value)

        return this + (name to updatedBinding)
    }

    private companion object {
        const val GLOBAL_SCOPE_COUNT = 1
    }
}
