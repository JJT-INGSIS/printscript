package printscript.v1.linter

import printscript.linter.LintRule
import printscript.linter.Linter
import printscript.linter.LinterFactory
import printscript.v1.linter.internal.configuration.PrintScriptV11LinterConfigurationReader
import printscript.v1.linter.rule.PrintScriptV11RecursiveStatementRule

public object PrintScriptV11LinterFactory {

    @JvmStatic
    public fun defaultConfiguration(): PrintScriptV11LinterConfiguration {
        return PrintScriptV11LinterConfiguration(
            rules = PrintScriptV1LinterFactory.defaultConfiguration().rules,
        )
    }

    @JvmStatic
    public fun configurationFrom(json: String): PrintScriptV11LinterConfigurationResult {
        return PrintScriptV11LinterConfigurationReader.read(json)
    }

    @JvmStatic
    @JvmOverloads
    public fun create(
        configuration: PrintScriptV11LinterConfiguration = defaultConfiguration(),
        additionalRules: List<LintRule> = emptyList(),
    ): Linter {
        return LinterFactory.create(
            rules = listOf(
                PrintScriptV11RecursiveStatementRule(
                    rules = additionalRules + PrintScriptV1LinterFactory.rulesFrom(configuration.rules),
                ),
            ),
        )
    }
}

internal fun variableOrLiteralReadInputArgumentConfiguration(): PrintScriptV1RuleConfiguration.ReadInputArgument {
    return PrintScriptV1RuleConfiguration.ReadInputArgument(
        acceptanceByKind = mapOf(
            PrintScriptExpressionKind.LITERAL to PrintScriptArgumentAcceptance.ACCEPTED,
            PrintScriptExpressionKind.VARIABLE to PrintScriptArgumentAcceptance.ACCEPTED,
            PrintScriptExpressionKind.COMPOSED to PrintScriptArgumentAcceptance.REJECTED,
        ),
    )
}
