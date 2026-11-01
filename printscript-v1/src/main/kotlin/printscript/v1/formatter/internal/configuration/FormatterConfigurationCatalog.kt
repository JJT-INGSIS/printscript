package printscript.v1.formatter.internal.configuration

import printscript.v1.configuration.ConfigurationRule
import printscript.v1.internal.configuration.ConfigurationRuleCatalog
import printscript.v1.internal.configuration.ConfigurationRuleChecker

internal object FormatterConfigurationCatalog {

    private val equalsSpacingConflict = setOf(
        "enforce-spacing-around-equals",
        "enforce-no-spacing-around-equals",
    )

    private val ifBracePlacementConflict = setOf(
        "if-brace-same-line",
        "if-brace-below-line",
    )

    val v1Rules: List<ConfigurationRule> = ConfigurationRuleCatalog.from(
        document = PrintScriptV1FormatterConfigurationDocument.serializer().descriptor,
        conflicts = listOf(equalsSpacingConflict),
    )

    val v11Rules: List<ConfigurationRule> = ConfigurationRuleCatalog.from(
        document = PrintScriptV11FormatterConfigurationDocument.serializer().descriptor,
        conflicts = listOf(equalsSpacingConflict, ifBracePlacementConflict),
    )

    val v1Checker: ConfigurationRuleChecker = ConfigurationRuleChecker(v1Rules)

    val v11Checker: ConfigurationRuleChecker = ConfigurationRuleChecker(v11Rules)
}
