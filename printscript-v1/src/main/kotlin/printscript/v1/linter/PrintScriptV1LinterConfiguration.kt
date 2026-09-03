package printscript.v1.linter

public class PrintScriptV1LinterConfiguration(
    rules: List<PrintScriptV1RuleConfiguration>,
) {

    public val rules: List<PrintScriptV1RuleConfiguration> = rules.toList()
}
