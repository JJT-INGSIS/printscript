package printscript.linter

/**
 * Las reglas activas, en orden. La que no está no se construye.
 */
public data class LinterConfiguration(
    public val rules: List<RuleConfiguration>,
)
