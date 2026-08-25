package printscript.linter

import printscript.linter.internal.DiagnosticSearch
import printscript.linter.internal.PrintScriptLinter
import printscript.linter.internal.rule.CompositeRule
import printscript.linter.internal.rule.IdentifierNamingRule
import printscript.linter.internal.rule.LintRule
import printscript.linter.internal.rule.PrintlnArgumentRule

public object PrintScriptLinterFactory {

    public fun defaultV1Configuration(): LinterConfiguration {
        return LinterConfiguration(
            rules = listOf(
                RuleConfiguration.IdentifierNaming(
                    convention = NamingConvention.CAMEL_CASE,
                ),
                RuleConfiguration.PrintlnArgument(
                    acceptanceByKind = mapOf(
                        ExpressionKind.LITERAL to ArgumentAcceptance.ACCEPTED,
                        ExpressionKind.VARIABLE to ArgumentAcceptance.ACCEPTED,
                        ExpressionKind.COMPOSED to ArgumentAcceptance.REJECTED,
                    ),
                ),
            ),
        )
    }

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
