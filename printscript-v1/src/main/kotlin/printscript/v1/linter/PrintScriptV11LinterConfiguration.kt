package printscript.v1.linter

public class PrintScriptV11LinterConfiguration(
    rules: List<PrintScriptV1RuleConfiguration>,
) {

    public val rules: List<PrintScriptV1RuleConfiguration> = rules.toList()
}
