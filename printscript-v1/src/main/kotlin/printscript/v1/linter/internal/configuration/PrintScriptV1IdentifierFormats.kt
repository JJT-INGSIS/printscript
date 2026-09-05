package printscript.v1.linter.internal.configuration

import printscript.v1.linter.PrintScriptV1NamingConvention

internal val namingConventionByConfiguredName: Map<String, PrintScriptV1NamingConvention> = mapOf(
    "camel case" to PrintScriptV1NamingConvention.CAMEL_CASE,
    "snake case" to PrintScriptV1NamingConvention.SNAKE_CASE,
)
