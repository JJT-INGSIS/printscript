package printscript.linter

import printscript.linter.internal.PrintScriptLinter
import printscript.linter.internal.rule.IdentifierNamingRule
import printscript.linter.internal.rule.LintRule
import printscript.linter.internal.rule.PrintlnArgumentRule

public object PrintScriptLinterFactory {

    public fun createV1(
        configuration: LinterConfiguration,
    ): Linter {
        return PrintScriptLinter(
            rules = configuration.rules.map { rule -> ruleFor(rule) },
        )
    }

    private fun ruleFor(
        configuration: RuleConfiguration,
    ): LintRule {
        return when (configuration) {
            is RuleConfiguration.IdentifierNaming ->
                IdentifierNamingRule(configuration.convention)

            RuleConfiguration.PrintlnArgument ->
                PrintlnArgumentRule()
        }
    }
}
