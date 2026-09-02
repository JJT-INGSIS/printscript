package printscript.runtime

/** Immutable collection of the variable bindings available during execution. */
public interface Environment {

    public fun lookupBinding(name: String): VariableBinding?

    public fun withBinding(name: String, binding: VariableBinding): Environment
}
