package printscript.runtime

import printscript.runtime.internal.environment.ScopedEnvironment

public object EnvironmentFactory {

    public fun empty(): Environment {
        return ScopedEnvironment()
    }
}
