package printscript.runtime

public fun interface EnvironmentVariableProvider {

    public fun valueOf(name: String): String?
}
