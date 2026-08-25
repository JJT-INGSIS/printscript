package printscript.linter

import printscript.linter.internal.DiagnosticSearch
import printscript.linter.internal.PrintScriptLinter
import printscript.linter.internal.rule.IdentifierNamingRule
import printscript.linter.internal.rule.LintRule
import printscript.linter.internal.rule.PrintlnArgumentRule
import printscript.linter.internal.rule.RuleSet

public object PrintScriptLinterFactory {

    public fun createV1(configuration: LinterConfiguration): Linter {
        return PrintScriptLinter(
            search = DiagnosticSearch(
                rules = ruleSetOf(configuration),
            ),
        )
    }

    private fun ruleSetOf(configuration: LinterConfiguration): RuleSet {
        return RuleSet(
            rules = configuration.rules.map { rule -> ruleFor(rule) },
        )
    }

    private fun ruleFor(configuration: RuleConfiguration): LintRule {
        return when (configuration) {
            is RuleConfiguration.IdentifierNaming ->
                IdentifierNamingRule(configuration.convention)

            is RuleConfiguration.PrintlnArgument ->
                PrintlnArgumentRule(configuration.acceptanceByKind)
        }
    }
}
