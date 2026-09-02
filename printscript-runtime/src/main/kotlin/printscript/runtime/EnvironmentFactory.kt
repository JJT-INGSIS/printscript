package printscript.runtime

import printscript.runtime.internal.environment.MapEnvironment

/** Creates environments without exposing their storage implementation. */
public object EnvironmentFactory {

    public fun empty(): Environment {
        return MapEnvironment()
    }
}
