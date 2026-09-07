package printscript.runtime

public interface Environment {

    public fun findBinding(name: String): VariableBinding?

    public fun findBindingInCurrentScope(name: String): VariableBinding?

    public fun declare(name: String, binding: VariableBinding): Environment

    public fun reassign(name: String, value: RuntimeValue): Environment

    public fun enterScope(): Environment

    public fun leaveScope(): Environment
}
