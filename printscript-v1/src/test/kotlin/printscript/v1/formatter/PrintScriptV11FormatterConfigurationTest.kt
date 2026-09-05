package printscript.v1.formatter

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class PrintScriptV11FormatterConfigurationTest {

    @Test
    fun `an empty document produces the default configuration`() {
        val result = PrintScriptV11FormatterFactory.configurationFrom("{}")
        val success = assertIs<PrintScriptV11FormatterConfigurationResult.Success>(result)

        assertEquals(
            expected = PrintScriptV11FormatterFactory.defaultConfiguration(),
            actual = success.configuration,
        )
    }

    @Test
    fun `maps V1 and V1_1 properties`() {
        val result = PrintScriptV11FormatterFactory.configurationFrom(
            """
            {
                "enforce-spacing-around-equals": true,
                "if-brace-below-line": true,
                "indent-inside-if": 4
            }
            """.trimIndent(),
        )
        val success = assertIs<PrintScriptV11FormatterConfigurationResult.Success>(result)

        assertEquals(
            expected = EqualsSpacing.SURROUNDED_BY_SPACES,
            actual = success.configuration.v1Configuration.equalsSpacing,
        )
        assertEquals(
            expected = IfBracePlacement.NEXT_LINE,
            actual = success.configuration.ifBracePlacement,
        )
        assertEquals(
            expected = 4u,
            actual = success.configuration.indentationInsideIf,
        )
    }

    @Test
    fun `a configuration read from JSON formats an if statement`() {
        val result = PrintScriptV11FormatterFactory.configurationFrom(
            """{"if-brace-below-line": true}""",
        )
        val success = assertIs<PrintScriptV11FormatterConfigurationResult.Success>(result)

        val formatted = formatSourceV11(
            sourceCode = "if (active) {\n}",
            configuration = success.configuration,
        )

        assertEquals(
            expected = "if (active)\n{\n}",
            actual = formatted,
        )
    }

    @Test
    fun `rejects contradictory brace placement properties`() {
        val result = PrintScriptV11FormatterFactory.configurationFrom(
            """
            {
                "if-brace-same-line": true,
                "if-brace-below-line": true
            }
            """.trimIndent(),
        )
        val failure = assertIs<PrintScriptV11FormatterConfigurationResult.Failure>(result)

        assertIs<PrintScriptV11FormatterConfigurationError.ConflictingIfBracePlacementRules>(
            failure.error,
        )
    }

    @Test
    fun `rejects negative indentation`() {
        val result = PrintScriptV11FormatterFactory.configurationFrom(
            """{"indent-inside-if": -1}""",
        )
        val failure = assertIs<PrintScriptV11FormatterConfigurationResult.Failure>(result)
        val error = assertIs<PrintScriptV11FormatterConfigurationError.NegativeIndentationSize>(
            failure.error,
        )

        assertEquals(-1, error.providedValue)
    }

    @Test
    fun `reports inherited V1 configuration failures`() {
        val result = PrintScriptV11FormatterFactory.configurationFrom(
            """
            {
                "enforce-no-spacing-around-equals": true,
                "enforce-spacing-around-equals": true
            }
            """.trimIndent(),
        )
        val failure = assertIs<PrintScriptV11FormatterConfigurationResult.Failure>(result)
        val error = assertIs<PrintScriptV11FormatterConfigurationError.V1ConfigurationFailure>(
            failure.error,
        )

        assertIs<PrintScriptV1FormatterConfigurationError.ConflictingEqualsSpacingRules>(error.error)
    }

    @Test
    fun `rejects malformed JSON`() {
        val result = PrintScriptV11FormatterFactory.configurationFrom("{ not valid json")
        val failure = assertIs<PrintScriptV11FormatterConfigurationResult.Failure>(result)

        assertIs<PrintScriptV11FormatterConfigurationError.InvalidConfigurationDocument>(
            failure.error,
        )
    }

    @Test
    fun `rejects unknown properties`() {
        val result = PrintScriptV11FormatterFactory.configurationFrom(
            """{"unknown-rule": true}""",
        )
        val failure = assertIs<PrintScriptV11FormatterConfigurationResult.Failure>(result)

        assertIs<PrintScriptV11FormatterConfigurationError.InvalidConfigurationDocument>(
            failure.error,
        )
    }

    @Test
    fun `rejects properties with the wrong JSON type`() {
        val result = PrintScriptV11FormatterFactory.configurationFrom(
            """{"indent-inside-if": "four"}""",
        )
        val failure = assertIs<PrintScriptV11FormatterConfigurationResult.Failure>(result)

        assertIs<PrintScriptV11FormatterConfigurationError.InvalidConfigurationDocument>(
            failure.error,
        )
    }

    @Test
    fun `V1 rejects V1_1 properties`() {
        val result = PrintScriptV1FormatterFactory.configurationFrom(
            """{"if-brace-same-line": true}""",
        )

        assertIs<PrintScriptV1FormatterConfigurationResult.Failure>(result)
    }
}
