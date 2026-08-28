package printscript.v1.interpreter

public interface PrintScriptV1Environment {

    public fun lookupBinding(name: String): PrintScriptV1VariableBinding?

    public fun withBinding(name: String, binding: PrintScriptV1VariableBinding): PrintScriptV1Environment
}
