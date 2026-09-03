package printscript.runtime

import printscript.runtime.internal.environment.MapEnvironment

public object EnvironmentFactory {

    public fun empty(): Environment {
        return MapEnvironment()
    }
}
