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
                "equalsSpacing": "WITHOUT_SPACES",
                "enforceSpaceBeforeColonInDeclaration": true,
                "enforceSpaceAfterColonInDeclaration": true,
                "enforceSingleSpaceSeparation": true,
                "enforceSpaceAroundBinaryOperators": true,
                "enforceLineBreakAfterStatement": true,
                "lineBreaksAfterPrintln": 2
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
        val json = """{"enforceSingleSpaceSeparation": true}"""

        val result = PrintScriptV1FormatterFactory.configurationFrom(json)

        val success = assertIs<PrintScriptV1FormatterConfigurationResult.Success>(result)

        assertEquals(expected = true, actual = success.configuration.enforceSingleSpaceSeparation)
        assertEquals(expected = false, actual = success.configuration.enforceSpaceAroundBinaryOperators)
        assertEquals(expected = null, actual = success.configuration.equalsSpacing)
    }

    @Test
    fun `an unknown key is ignored`() {
        val json = """{"thisKeyDoesNotExist": true, "enforceSingleSpaceSeparation": true}"""

        val result = PrintScriptV1FormatterFactory.configurationFrom(json)

        val success = assertIs<PrintScriptV1FormatterConfigurationResult.Success>(result)

        assertEquals(expected = true, actual = success.configuration.enforceSingleSpaceSeparation)
    }

    @Test
    fun `malformed JSON is reported as a domain failure instead of throwing`() {
        val result = PrintScriptV1FormatterFactory.configurationFrom("{ not valid json")

        val failure = assertIs<PrintScriptV1FormatterConfigurationResult.Failure>(result)

        assertIs<PrintScriptV1FormatterConfigurationError.MalformedJson>(failure.error)
    }

    @Test
    fun `an unsupported equals spacing value is reported with the supported values`() {
        val json = """{"equalsSpacing": "SIDEWAYS"}"""

        val result = PrintScriptV1FormatterFactory.configurationFrom(json)

        val failure = assertIs<PrintScriptV1FormatterConfigurationResult.Failure>(result)
        val error = assertIs<PrintScriptV1FormatterConfigurationError.UnknownEqualsSpacing>(failure.error)

        assertEquals(expected = "SIDEWAYS", actual = error.value)
        assertEquals(
            expected = setOf("SURROUNDED_BY_SPACES", "WITHOUT_SPACES"),
            actual = error.supportedValues,
        )
    }

    @Test
    fun `a negative line break count is reported instead of accepted`() {
        val json = """{"lineBreaksAfterPrintln": -1}"""

        val result = PrintScriptV1FormatterFactory.configurationFrom(json)

        val failure = assertIs<PrintScriptV1FormatterConfigurationResult.Failure>(result)
        val error = assertIs<PrintScriptV1FormatterConfigurationError.NegativeLineBreakCount>(failure.error)

        assertEquals(expected = -1, actual = error.providedValue)
    }

    @Test
    fun `a configuration read from JSON formats the same as its Kotlin equivalent`() {
        val source = "let value :number= 1;"
        val json = """{"enforceSingleSpaceSeparation": true}"""

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
