package printscript.v1.linter

import printscript.linter.LintRule
import printscript.linter.Linter
import printscript.linter.LinterFactory
import printscript.v1.linter.internal.configuration.PrintScriptV1LinterConfigurationReader
import printscript.v1.linter.rule.PrintScriptV1IdentifierNamingRule
import printscript.v1.linter.rule.PrintScriptV1PrintlnArgumentRule

public object PrintScriptV1LinterFactory {

    @JvmStatic
    public fun defaultConfiguration(): PrintScriptV1LinterConfiguration {
        return PrintScriptV1LinterConfiguration(
            rules = listOf(
                PrintScriptV1RuleConfiguration.IdentifierNaming(
                    convention = PrintScriptV1NamingConvention.CAMEL_CASE,
                ),
                variableOrLiteralPrintlnArgumentRule(),
            ),
        )
    }

    @JvmStatic
    public fun configurationFrom(json: String): PrintScriptV1LinterConfigurationResult {
        return PrintScriptV1LinterConfigurationReader.read(json)
    }

    @JvmStatic
    @JvmOverloads
    public fun create(
        configuration: PrintScriptV1LinterConfiguration = defaultConfiguration(),
        additionalRules: List<LintRule> = emptyList(),
    ): Linter {
        return LinterFactory.create(
            rules = additionalRules + printScriptV1Rules(configuration),
        )
    }

    private fun printScriptV1Rules(configuration: PrintScriptV1LinterConfiguration): List<LintRule> {
        return configuration.rules.map { rule -> ruleFor(rule) }
    }

    private fun ruleFor(configuration: PrintScriptV1RuleConfiguration): LintRule {
        return when (configuration) {
            is PrintScriptV1RuleConfiguration.IdentifierNaming ->
                PrintScriptV1IdentifierNamingRule(configuration.convention)

            is PrintScriptV1RuleConfiguration.PrintlnArgument ->
                PrintScriptV1PrintlnArgumentRule(configuration.acceptanceByKind)
        }
    }
}

internal fun variableOrLiteralPrintlnArgumentRule(): PrintScriptV1RuleConfiguration.PrintlnArgument {
    return PrintScriptV1RuleConfiguration.PrintlnArgument(
        acceptanceByKind = mapOf(
            PrintScriptV1ExpressionKind.LITERAL to PrintScriptV1ArgumentAcceptance.ACCEPTED,
            PrintScriptV1ExpressionKind.VARIABLE to PrintScriptV1ArgumentAcceptance.ACCEPTED,
            PrintScriptV1ExpressionKind.COMPOSED to PrintScriptV1ArgumentAcceptance.REJECTED,
        ),
    )
}
