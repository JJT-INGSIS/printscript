package printscript.runtime

public interface Environment {

    public fun lookupBinding(name: String): VariableBinding?

    public fun withBinding(name: String, binding: VariableBinding): Environment
}
