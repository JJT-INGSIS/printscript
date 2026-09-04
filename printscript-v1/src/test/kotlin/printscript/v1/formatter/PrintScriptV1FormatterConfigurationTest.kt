package printscript.v1.formatter

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class PrintScriptV1FormatterConfigurationTest {

    @Test
    fun `an empty document produces the default configuration`() {
        val result = PrintScriptV1FormatterFactory.configurationFrom("{}")

        val success = assertIs<PrintScriptV1FormatterConfigurationResult.Success>(result)

        assertEquals(
            expected = PrintScriptV1FormatterFactory.defaultConfiguration(),
            actual = success.configuration,
        )
    }

    @Test
    fun `maps every known key to its field`() {
        val json =
            """
            {
                "enforce-no-spacing-around-equals": true,
                "enforce-spacing-before-colon-in-declaration": true,
                "enforce-spacing-after-colon-in-declaration": true,
                "mandatory-single-space-separation": true,
                "mandatory-space-surrounding-operations": true,
                "mandatory-line-break-after-statement": true,
                "line-breaks-after-println": 2
            }
            """.trimIndent()

        val result = PrintScriptV1FormatterFactory.configurationFrom(json)

        val success = assertIs<PrintScriptV1FormatterConfigurationResult.Success>(result)

        assertEquals(
            expected = PrintScriptV1FormatterConfiguration(
                equalsSpacing = EqualsSpacing.WITHOUT_SPACES,
                enforceSpaceBeforeColonInDeclaration = true,
                enforceSpaceAfterColonInDeclaration = true,
                enforceSingleSpaceSeparation = true,
                enforceSpaceAroundBinaryOperators = true,
                enforceLineBreakAfterStatement = true,
                lineBreaksAfterPrintln = 2u,
            ),
            actual = success.configuration,
        )
    }

    @Test
    fun `an absent key keeps the default for that field`() {
        val json = """{"mandatory-single-space-separation": true}"""

        val result = PrintScriptV1FormatterFactory.configurationFrom(json)

        val success = assertIs<PrintScriptV1FormatterConfigurationResult.Success>(result)

        assertEquals(expected = true, actual = success.configuration.enforceSingleSpaceSeparation)
        assertEquals(expected = false, actual = success.configuration.enforceSpaceAroundBinaryOperators)
        assertEquals(expected = null, actual = success.configuration.equalsSpacing)
    }

    @Test
    fun `an unknown key is rejected`() {
        val json = """{"this-key-does-not-exist": true}"""

        val result = PrintScriptV1FormatterFactory.configurationFrom(json)

        val failure = assertIs<PrintScriptV1FormatterConfigurationResult.Failure>(result)
        assertIs<PrintScriptV1FormatterConfigurationError.InvalidConfigurationDocument>(failure.error)
    }

    @Test
    fun `malformed JSON is reported as a domain failure instead of throwing`() {
        val result = PrintScriptV1FormatterFactory.configurationFrom("{ not valid json")

        val failure = assertIs<PrintScriptV1FormatterConfigurationResult.Failure>(result)

        assertIs<PrintScriptV1FormatterConfigurationError.InvalidConfigurationDocument>(failure.error)
    }

    @Test
    fun `maps the spacing around equals rule`() {
        val json = """{"enforce-spacing-around-equals": true}"""

        val result = PrintScriptV1FormatterFactory.configurationFrom(json)

        val success = assertIs<PrintScriptV1FormatterConfigurationResult.Success>(result)

        assertEquals(
            expected = EqualsSpacing.SURROUNDED_BY_SPACES,
            actual = success.configuration.equalsSpacing,
        )
    }

    @Test
    fun `rejects contradictory equals spacing rules`() {
        val json =
            """
            {
                "enforce-no-spacing-around-equals": true,
                "enforce-spacing-around-equals": true
            }
            """.trimIndent()

        val result = PrintScriptV1FormatterFactory.configurationFrom(json)

        val failure = assertIs<PrintScriptV1FormatterConfigurationResult.Failure>(result)
        assertIs<PrintScriptV1FormatterConfigurationError.ConflictingEqualsSpacingRules>(failure.error)
    }

    @Test
    fun `a value with the wrong JSON type is rejected`() {
        val json = """{"mandatory-single-space-separation": "yes"}"""

        val result = PrintScriptV1FormatterFactory.configurationFrom(json)

        val failure = assertIs<PrintScriptV1FormatterConfigurationResult.Failure>(result)
        assertIs<PrintScriptV1FormatterConfigurationError.InvalidConfigurationDocument>(failure.error)
    }

    @Test
    fun `a negative line break count is reported instead of accepted`() {
        val json = """{"line-breaks-after-println": -1}"""

        val result = PrintScriptV1FormatterFactory.configurationFrom(json)

        val failure = assertIs<PrintScriptV1FormatterConfigurationResult.Failure>(result)
        val error = assertIs<PrintScriptV1FormatterConfigurationError.NegativeLineBreakCount>(failure.error)

        assertEquals(expected = -1, actual = error.providedValue)
    }

    @Test
    fun `a configuration read from JSON formats the same as its Kotlin equivalent`() {
        val source = "let value :number= 1;"
        val json = """{"mandatory-single-space-separation": true}"""

        val result = PrintScriptV1FormatterFactory.configurationFrom(json)
        val success = assertIs<PrintScriptV1FormatterConfigurationResult.Success>(result)

        val formattedFromJson = formatSource(source, configuration = success.configuration)
        val formattedFromKotlin = formatSource(
            source,
            configuration = PrintScriptV1FormatterConfiguration(enforceSingleSpaceSeparation = true),
        )

        assertEquals(expected = formattedFromKotlin, actual = formattedFromJson)
    }
}
