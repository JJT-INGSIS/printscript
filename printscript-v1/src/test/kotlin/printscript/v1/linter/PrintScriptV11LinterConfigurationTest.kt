package printscript.v1.linter

import printscript.ast.DeclaredType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class PrintScriptV11LinterConfigurationTest {

    @Test
    fun `an empty document produces a configuration without rules`() {
        val configuration = configurationFrom("{}")

        assertEquals(expected = emptyList(), actual = configuration.rules)
    }

    @Test
    fun `still maps the shared identifier format property`() {
        val configuration = configurationFrom(
            """{"identifier_format": "snake case"}""",
        )

        val rule = assertIs<PrintScriptV1RuleConfiguration.IdentifierNaming>(
            configuration.rules.single(),
        )
        assertEquals(expected = PrintScriptV1NamingConvention.SNAKE_CASE, actual = rule.convention)
    }

    @Test
    fun `still maps the shared mandatory println argument property`() {
        val configuration = configurationFrom(
            """{"mandatory-variable-or-literal-in-println": true}""",
        )

        assertIs<PrintScriptV1RuleConfiguration.PrintlnArgument>(configuration.rules.single())
    }

    @Test
    fun `maps the mandatory readInput argument rule when true`() {
        val configuration = configurationFrom(
            """{"mandatory-variable-or-literal-in-readInput": true}""",
        )

        val rule = assertIs<PrintScriptV1RuleConfiguration.ReadInputArgument>(
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
    fun `a disabled readInput argument rule is left out`() {
        val configuration = configurationFrom(
            """{"mandatory-variable-or-literal-in-readInput": false}""",
        )

        assertEquals(expected = emptyList(), actual = configuration.rules)
    }

    @Test
    fun `an absent readInput property is left out`() {
        val configuration = configurationFrom(
            """{"identifier_format": "camel case"}""",
        )

        val expectedRule = PrintScriptV1RuleConfiguration.IdentifierNaming(
            convention = PrintScriptV1NamingConvention.CAMEL_CASE,
        )

        assertEquals(
            expected = listOf(expectedRule),
            actual = configuration.rules,
        )
    }

    @Test
    fun `an unknown identifier format is reported as a domain failure`() {
        val result = PrintScriptV11LinterFactory.configurationFrom(
            """{"identifier_format": "kebab case"}""",
        )

        val failure = assertIs<PrintScriptV11LinterConfigurationResult.Failure>(result)
        val error = assertIs<PrintScriptV11LinterConfigurationError.UnknownIdentifierFormat>(failure.error)
        assertEquals(expected = "kebab case", actual = error.providedValue)
    }

    @Test
    fun `an unknown key is rejected`() {
        val result = PrintScriptV11LinterFactory.configurationFrom(
            """{"unknown-rule": true}""",
        )

        val failure = assertIs<PrintScriptV11LinterConfigurationResult.Failure>(result)
        assertIs<PrintScriptV11LinterConfigurationError.InvalidConfigurationDocument>(failure.error)
    }

    @Test
    fun `malformed JSON is reported as a domain failure`() {
        val result = PrintScriptV11LinterFactory.configurationFrom("{ not valid json")

        val failure = assertIs<PrintScriptV11LinterConfigurationResult.Failure>(result)
        assertIs<PrintScriptV11LinterConfigurationError.InvalidConfigurationDocument>(failure.error)
    }

    @Test
    fun `a value with the wrong JSON type is rejected`() {
        val result = PrintScriptV11LinterFactory.configurationFrom(
            """{"mandatory-variable-or-literal-in-readInput": "yes"}""",
        )

        val failure = assertIs<PrintScriptV11LinterConfigurationResult.Failure>(result)
        assertIs<PrintScriptV11LinterConfigurationError.InvalidConfigurationDocument>(failure.error)
    }

    @Test
    fun `V1 continues rejecting the V1_1-only property`() {
        val result = PrintScriptV1LinterFactory.configurationFrom(
            """{"mandatory-variable-or-literal-in-readInput": true}""",
        )

        val failure = assertIs<PrintScriptV1LinterConfigurationResult.Failure>(result)
        assertIs<PrintScriptV1LinterConfigurationError.InvalidConfigurationDocument>(failure.error)
    }

    @Test
    fun `a configuration read from JSON controls the V1_1 linter`() {
        val configuration = configurationFrom(
            """{"mandatory-variable-or-literal-in-readInput": true}""",
        )
        val linter = PrintScriptV11LinterFactory.create(configuration)

        val diagnostics = diagnosticsOf(
            linter,
            declare("input", DeclaredType.STRING, readInput(sum(text("a"), text("b")))),
        )

        assertIs<PrintScriptV11Diagnostic.UnsupportedReadInputArgument>(diagnostics.single())
    }

    @Test
    fun `a hand-built V1 configuration can still smuggle the readInput rule into the V1 factory`() {
        val configuration = PrintScriptV1LinterConfiguration(
            rules = listOf(readInputArgumentRule()),
        )
        val linter = PrintScriptV1LinterFactory.create(configuration)

        val diagnostics = diagnosticsOf(
            linter,
            declare("input", DeclaredType.STRING, readInput(sum(text("a"), text("b")))),
        )

        assertIs<PrintScriptV11Diagnostic.UnsupportedReadInputArgument>(diagnostics.single())
    }

    private fun configurationFrom(json: String): PrintScriptV11LinterConfiguration {
        val result = PrintScriptV11LinterFactory.configurationFrom(json)
        val success = assertIs<PrintScriptV11LinterConfigurationResult.Success>(result)

        return success.configuration
    }
}
