package printscript.v1.configuration

import printscript.v1.formatter.configuration.PrintScriptV11FormatterConfiguration
import printscript.v1.formatter.configuration.PrintScriptV1FormatterConfiguration
import printscript.v1.linter.PrintScriptV11LinterFactory
import printscript.v1.linter.PrintScriptV1LinterFactory
import kotlin.test.Test
import kotlin.test.assertEquals

class ConfigurationRuleCatalogTest {

    private val v1FormatterRules = listOf(
        ConfigurationRule(
            id = "enforce-no-spacing-around-equals",
            type = ConfigurationRuleType.BOOLEAN,
            acceptsNull = false,
            conflictsWith = setOf("enforce-spacing-around-equals"),
        ),
        ConfigurationRule(
            id = "enforce-spacing-around-equals",
            type = ConfigurationRuleType.BOOLEAN,
            acceptsNull = false,
            conflictsWith = setOf("enforce-no-spacing-around-equals"),
        ),
        flag("enforce-spacing-before-colon-in-declaration"),
        flag("enforce-spacing-after-colon-in-declaration"),
        flag("mandatory-single-space-separation"),
        flag("mandatory-space-surrounding-operations"),
        flag("mandatory-line-break-after-statement"),
        ConfigurationRule("line-breaks-after-println", ConfigurationRuleType.INTEGER, acceptsNull = true),
    )

    @Test
    fun `the 1_0 formatter describes every rule it reads`() {
        assertEquals(v1FormatterRules, PrintScriptV1FormatterConfiguration.rules())
    }

    @Test
    fun `the 1_1 formatter adds the if rules to the 1_0 ones`() {
        val expected = v1FormatterRules + listOf(
            ConfigurationRule(
                id = "if-brace-same-line",
                type = ConfigurationRuleType.BOOLEAN,
                acceptsNull = false,
                conflictsWith = setOf("if-brace-below-line"),
            ),
            ConfigurationRule(
                id = "if-brace-below-line",
                type = ConfigurationRuleType.BOOLEAN,
                acceptsNull = false,
                conflictsWith = setOf("if-brace-same-line"),
            ),
            ConfigurationRule("indent-inside-if", ConfigurationRuleType.INTEGER, acceptsNull = true),
            flag("mandatory-line-breaks-inside-if"),
        )

        assertEquals(expected, PrintScriptV11FormatterConfiguration.rules())
    }

    @Test
    fun `the linter describes its rules and the accepted identifier formats`() {
        val identifierFormat = ConfigurationRule(
            id = "identifier_format",
            type = ConfigurationRuleType.CHOICE,
            acceptsNull = true,
            choices = listOf("camel case", "snake case"),
        )
        val v1Rules = listOf(identifierFormat, flag("mandatory-variable-or-literal-in-println"))

        assertEquals(v1Rules, PrintScriptV1LinterFactory.configurationRules())
        assertEquals(
            v1Rules + flag("mandatory-variable-or-literal-in-readInput"),
            PrintScriptV11LinterFactory.configurationRules(),
        )
    }

    private fun flag(id: String): ConfigurationRule {
        return ConfigurationRule(id, ConfigurationRuleType.BOOLEAN, acceptsNull = false)
    }
}
