package printscript.v1.linter.internal.configuration

import printscript.v1.configuration.ConfigurationRule
import printscript.v1.internal.configuration.ConfigurationRuleCatalog
import printscript.v1.internal.configuration.ConfigurationRuleChecker

internal object LinterConfigurationCatalog {

    private val identifierFormats = mapOf(
        "identifier_format" to namingConventionByConfiguredName.keys.toList(),
    )

    val v1Rules: List<ConfigurationRule> = ConfigurationRuleCatalog.from(
        document = PrintScriptV1LinterConfigurationDocument.serializer().descriptor,
        choices = identifierFormats,
    )

    val v11Rules: List<ConfigurationRule> = ConfigurationRuleCatalog.from(
        document = PrintScriptV11LinterConfigurationDocument.serializer().descriptor,
        choices = identifierFormats,
    )

    val v1Checker: ConfigurationRuleChecker = ConfigurationRuleChecker(v1Rules)

    val v11Checker: ConfigurationRuleChecker = ConfigurationRuleChecker(v11Rules)
}
