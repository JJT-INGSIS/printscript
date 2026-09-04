package printscript.runtime

public interface Environment {

    public fun lookupBinding(name: String): VariableBinding?

    public fun declaring(name: String, binding: VariableBinding): Environment

    public fun reassigning(name: String, value: RuntimeValue): Environment

    public fun enteringScope(): Environment

    public fun leavingScope(): Environment
}
