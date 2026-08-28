package printscript.v1.linter

/**
 * Las reglas activas, en orden. La que no está no se construye.
 */
public class PrintScriptV1LinterConfiguration(
    rules: List<PrintScriptV1RuleConfiguration>,
) {

    public val rules: List<PrintScriptV1RuleConfiguration> = rules.toList()
}
