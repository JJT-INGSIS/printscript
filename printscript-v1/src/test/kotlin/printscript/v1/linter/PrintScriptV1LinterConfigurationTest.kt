package printscript.v1.linter

import printscript.ast.DeclaredType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class PrintScriptV1LinterConfigurationTest {

    @Test
    fun `an empty document produces a configuration without rules`() {
        val configuration = configurationFrom("{}")

        assertEquals(expected = emptyList(), actual = configuration.rules)
    }

    @Test
    fun `maps the camel case identifier format`() {
        val configuration = configurationFrom(
            """{"identifier_format": "camel case"}""",
        )

        val rule = assertIs<PrintScriptV1RuleConfiguration.IdentifierNaming>(
            configuration.rules.single(),
        )
        assertEquals(expected = PrintScriptV1NamingConvention.CAMEL_CASE, actual = rule.convention)
    }

    @Test
    fun `maps the snake case identifier format`() {
        val configuration = configurationFrom(
            """{"identifier_format": "snake case"}""",
        )

        val rule = assertIs<PrintScriptV1RuleConfiguration.IdentifierNaming>(
            configuration.rules.single(),
        )
        assertEquals(expected = PrintScriptV1NamingConvention.SNAKE_CASE, actual = rule.convention)
    }

    @Test
    fun `maps the mandatory println argument rule`() {
        val configuration = configurationFrom(
            """{"mandatory-variable-or-literal-in-println": true}""",
        )

        val rule = assertIs<PrintScriptV1RuleConfiguration.PrintlnArgument>(
            configuration.rules.single(),
        )
        assertEquals(
            expected = mapOf(
                PrintScriptV1ExpressionKind.LITERAL to PrintScriptV1ArgumentAcceptance.ACCEPTED,
                PrintScriptV1ExpressionKind.VARIABLE to PrintScriptV1ArgumentAcceptance.ACCEPTED,
                PrintScriptV1ExpressionKind.COMPOSED to PrintScriptV1ArgumentAcceptance.REJECTED,
            ),
            actual = rule.acceptanceByKind,
        )
    }

    @Test
    fun `a disabled println argument rule is left out`() {
        val configuration = configurationFrom(
            """{"mandatory-variable-or-literal-in-println": false}""",
        )

        assertEquals(expected = emptyList(), actual = configuration.rules)
    }

    @Test
    fun `an unknown identifier format is reported as a domain failure`() {
        val result = PrintScriptV1LinterFactory.configurationFrom(
            """{"identifier_format": "kebab case"}""",
        )

        val failure = assertIs<PrintScriptV1LinterConfigurationResult.Failure>(result)
        val error = assertIs<PrintScriptV1LinterConfigurationError.UnknownIdentifierFormat>(failure.error)
        assertEquals(expected = "kebab case", actual = error.providedValue)
    }

    @Test
    fun `an unknown key is rejected`() {
        val result = PrintScriptV1LinterFactory.configurationFrom(
            """{"unknown-rule": true}""",
        )

        val failure = assertIs<PrintScriptV1LinterConfigurationResult.Failure>(result)
        assertIs<PrintScriptV1LinterConfigurationError.InvalidConfigurationDocument>(failure.error)
    }

    @Test
    fun `malformed JSON is reported as a domain failure`() {
        val result = PrintScriptV1LinterFactory.configurationFrom("{ not valid json")

        val failure = assertIs<PrintScriptV1LinterConfigurationResult.Failure>(result)
        assertIs<PrintScriptV1LinterConfigurationError.InvalidConfigurationDocument>(failure.error)
    }

    @Test
    fun `a value with the wrong JSON type is rejected`() {
        val result = PrintScriptV1LinterFactory.configurationFrom(
            """{"mandatory-variable-or-literal-in-println": "yes"}""",
        )

        val failure = assertIs<PrintScriptV1LinterConfigurationResult.Failure>(result)
        assertIs<PrintScriptV1LinterConfigurationError.InvalidConfigurationDocument>(failure.error)
    }

    @Test
    fun `a configuration read from JSON controls the linter`() {
        val configuration = configurationFrom(
            """{"identifier_format": "camel case"}""",
        )
        val linter = PrintScriptV1LinterFactory.create(configuration)

        val diagnostics = diagnosticsOf(
            linter,
            declare("my_total", DeclaredType.NUMBER, number("1")),
        )

        assertIs<PrintScriptV1Diagnostic.NamingConventionViolation>(diagnostics.single())
    }

    private fun configurationFrom(json: String): PrintScriptV1LinterConfiguration {
        val result = PrintScriptV1LinterFactory.configurationFrom(json)
        val success = assertIs<PrintScriptV1LinterConfigurationResult.Success>(result)

        return success.configuration
    }
}
