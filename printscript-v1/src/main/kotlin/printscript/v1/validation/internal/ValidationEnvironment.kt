package printscript.v1.validation.internal

import printscript.ast.Identifier
import printscript.interpreter.ExecutionResult
import printscript.v1.interpreter.PrintScriptV1SemanticError
import printscript.v1.interpreter.internal.orReturn

internal class ValidationEnvironment private constructor(
    private val scopes: List<Map<String, ValidationBinding>>,
) {

    constructor() : this(listOf(emptyMap()))

    fun containsInCurrentScope(name: String): Boolean {
        return name in scopes.last()
    }

    fun bindingOf(identifier: Identifier): ExecutionResult<ValidationBinding> {
        val binding = scopes.asReversed().firstNotNullOfOrNull { it[identifier.value] }
            ?: return ExecutionResult.Failure(
                PrintScriptV1SemanticError.UndeclaredVariable(identifier.value, identifier.span),
            )
        return ExecutionResult.Success(binding)
    }

    fun initializedBindingOf(identifier: Identifier): ExecutionResult<ValidationBinding> {
        val binding = bindingOf(identifier).orReturn { return it }
        return if (binding.initialized) {
            ExecutionResult.Success(binding)
        } else {
            ExecutionResult.Failure(
                PrintScriptV1SemanticError.UninitializedVariable(identifier.value, identifier.span),
            )
        }
    }

    fun declaring(name: String, binding: ValidationBinding): ValidationEnvironment {
        return ValidationEnvironment(scopes.dropLast(1) + (scopes.last() + (name to binding)))
    }

    fun initializing(name: String): ValidationEnvironment {
        val targetScope = scopes.indexOfLast { name in it }
        check(targetScope >= 0) {
            "Se marco como inicializada la variable '$name', que no esta declarada en ningun scope"
        }
        return ValidationEnvironment(
            scopes.mapIndexed { index, scope ->
                if (index == targetScope) {
                    scope + (name to scope.getValue(name).copy(initialized = true))
                } else {
                    scope
                }
            },
        )
    }

    fun enteringScope(): ValidationEnvironment {
        return ValidationEnvironment(scopes + emptyMap())
    }

    fun leavingScope(): ValidationEnvironment {
        check(scopes.size > 1) { "Se intento salir del scope global" }
        return ValidationEnvironment(scopes.dropLast(1))
    }

    fun intersectingInitialization(other: ValidationEnvironment): ValidationEnvironment {
        require(scopes.size == other.scopes.size) {
            "Los entornos a intersecar tienen distinta cantidad de scopes: ${scopes.size} y ${other.scopes.size}"
        }
        return ValidationEnvironment(
            scopes.zip(other.scopes) { scope, otherScope ->
                require(scope.keys == otherScope.keys) {
                    "Los entornos a intersecar declaran variables distintas en un mismo scope: " +
                        "${scope.keys} y ${otherScope.keys}"
                }
                scope.mapValues { (name, binding) ->
                    binding.copy(initialized = binding.initialized && otherScope.getValue(name).initialized)
                }
            },
        )
    }
}
