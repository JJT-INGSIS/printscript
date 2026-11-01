package printscript.v1.configuration

import printscript.v1.configuration.ConfigurationRuleViolationReason.CONFLICTING_RULES
import printscript.v1.configuration.ConfigurationRuleViolationReason.OUT_OF_RANGE
import printscript.v1.configuration.ConfigurationRuleViolationReason.UNKNOWN_CHOICE
import printscript.v1.configuration.ConfigurationRuleViolationReason.UNKNOWN_RULE
import printscript.v1.configuration.ConfigurationRuleViolationReason.WRONG_TYPE
import printscript.v1.formatter.configuration.PrintScriptV11FormatterConfiguration
import printscript.v1.formatter.configuration.PrintScriptV1FormatterConfiguration
import printscript.v1.linter.PrintScriptV11LinterFactory
import printscript.v1.linter.PrintScriptV1LinterFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ConfigurationRuleCheckerTest {

    @Test
    fun `a document with known rules and valid values is valid`() {
        val json =
            """
            {
                "enforce-spacing-around-equals": true,
                "enforce-no-spacing-around-equals": false,
                "line-breaks-after-println": 2,
                "indent-inside-if": null
            }
            """.trimIndent()

        assertEquals(ConfigurationCheck.Valid, PrintScriptV11FormatterConfiguration.check(json))
        assertEquals(ConfigurationCheck.Valid, PrintScriptV1FormatterConfiguration.check("{}"))
    }

    @Test
    fun `every invalid rule is reported at once with its reason`() {
        val json =
            """
            {
                "unknown": true,
                "if-brace-same-line": true,
                "enforce-spacing-around-equals": "true",
                "mandatory-single-space-separation": 1,
                "mandatory-line-break-after-statement": null,
                "line-breaks-after-println": 1.5
            }
            """.trimIndent()

        assertEquals(
            ConfigurationCheck.InvalidRules(
                listOf(
                    violation("unknown", UNKNOWN_RULE),
                    violation("if-brace-same-line", UNKNOWN_RULE),
                    violation("enforce-spacing-around-equals", WRONG_TYPE),
                    violation("mandatory-single-space-separation", WRONG_TYPE),
                    violation("mandatory-line-break-after-statement", WRONG_TYPE),
                    violation("line-breaks-after-println", WRONG_TYPE),
                ),
            ),
            PrintScriptV1FormatterConfiguration.check(json),
        )
    }

    @Test
    fun `values are taken as written so quoted and exponent values are the wrong type`() {
        assertEquals(
            ConfigurationCheck.InvalidRules(
                listOf(
                    violation("mandatory-single-space-separation", WRONG_TYPE),
                    violation("line-breaks-after-println", WRONG_TYPE),
                    violation("indent-inside-if", WRONG_TYPE),
                ),
            ),
            PrintScriptV11FormatterConfiguration.check(
                """
                {
                    "mandatory-single-space-separation": "true",
                    "line-breaks-after-println": "1",
                    "indent-inside-if": 1e2
                }
                """.trimIndent(),
            ),
        )
    }

    @Test
    fun `integer rules accept from zero up to the largest int`() {
        assertEquals(
            ConfigurationCheck.InvalidRules(listOf(violation("line-breaks-after-println", OUT_OF_RANGE))),
            PrintScriptV1FormatterConfiguration.check("""{"line-breaks-after-println": -1}"""),
        )
        assertEquals(
            ConfigurationCheck.InvalidRules(listOf(violation("indent-inside-if", OUT_OF_RANGE))),
            PrintScriptV11FormatterConfiguration.check("""{"indent-inside-if": 2147483648}"""),
        )
        assertEquals(
            ConfigurationCheck.Valid,
            PrintScriptV11FormatterConfiguration.check("""{"indent-inside-if": 2147483647}"""),
        )
    }

    @Test
    fun `both rules of an enabled conflict are reported`() {
        assertEquals(
            ConfigurationCheck.InvalidRules(
                listOf(
                    violation("enforce-no-spacing-around-equals", CONFLICTING_RULES),
                    violation("enforce-spacing-around-equals", CONFLICTING_RULES),
                    violation("if-brace-same-line", CONFLICTING_RULES),
                    violation("if-brace-below-line", CONFLICTING_RULES),
                ),
            ),
            PrintScriptV11FormatterConfiguration.check(
                """
                {
                    "if-brace-below-line": true,
                    "if-brace-same-line": true,
                    "enforce-spacing-around-equals": true,
                    "enforce-no-spacing-around-equals": true
                }
                """.trimIndent(),
            ),
        )
    }

    @Test
    fun `a conflict needs both rules turned on`() {
        assertEquals(
            ConfigurationCheck.Valid,
            PrintScriptV11FormatterConfiguration.check(
                """{"if-brace-same-line": true, "if-brace-below-line": false}""",
            ),
        )
    }

    @Test
    fun `a choice rule only accepts one of its declared texts`() {
        assertEquals(
            ConfigurationCheck.Valid,
            PrintScriptV1LinterFactory.checkConfiguration("""{"identifier_format": "snake case"}"""),
        )
        assertEquals(
            ConfigurationCheck.Valid,
            PrintScriptV1LinterFactory.checkConfiguration("""{"identifier_format": null}"""),
        )
        assertEquals(
            ConfigurationCheck.InvalidRules(
                listOf(
                    violation("identifier_format", UNKNOWN_CHOICE),
                    violation("mandatory-variable-or-literal-in-readInput", WRONG_TYPE),
                ),
            ),
            PrintScriptV11LinterFactory.checkConfiguration(
                """{"identifier_format": "kebab case", "mandatory-variable-or-literal-in-readInput": "yes"}""",
            ),
        )
        assertEquals(
            ConfigurationCheck.InvalidRules(listOf(violation("identifier_format", WRONG_TYPE))),
            PrintScriptV1LinterFactory.checkConfiguration("""{"identifier_format": true}"""),
        )
    }

    @Test
    fun `a document that is not a JSON object is malformed`() {
        assertIs<ConfigurationCheck.MalformedDocument>(PrintScriptV1FormatterConfiguration.check("{ not json"))
        assertIs<ConfigurationCheck.MalformedDocument>(PrintScriptV11LinterFactory.checkConfiguration("[]"))
    }

    private fun violation(rule: String, reason: ConfigurationRuleViolationReason): ConfigurationRuleViolation {
        return ConfigurationRuleViolation(rule, reason)
    }
}
