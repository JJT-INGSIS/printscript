package printscript.linter

import printscript.linter.internal.DiagnosticSearch
import printscript.linter.internal.PrintScriptLinter
import printscript.linter.internal.rule.CompositeRule
import printscript.linter.internal.rule.IdentifierNamingRule
import printscript.linter.internal.rule.LintRule
import printscript.linter.internal.rule.PrintlnArgumentRule

public object PrintScriptLinterFactory {

    public fun createV1(configuration: LinterConfiguration): Linter {
        return PrintScriptLinter(
            search = DiagnosticSearch(
                rule = compositeOf(configuration),
            ),
        )
    }

    private fun compositeOf(configuration: LinterConfiguration): LintRule {
        return CompositeRule(
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
