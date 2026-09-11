package printscript.cli

import printscript.cli.internal.report.ConfigurationErrorReporter
import printscript.v1.formatter.configuration.PrintScriptV11FormatterConfigurationError
import printscript.v1.formatter.configuration.PrintScriptV1FormatterConfigurationError
import printscript.v1.linter.PrintScriptV11LinterConfigurationError
import printscript.v1.linter.PrintScriptV1LinterConfigurationError
import kotlin.test.Test
import kotlin.test.assertContains

class ConfigurationErrorReporterTest {

    private val reporter = ConfigurationErrorReporter()

    @Test
    fun `keeps the specific reason of a malformed formatter configuration`() {
        val message = reporter.describe(
            PrintScriptV1FormatterConfigurationError.InvalidConfigurationDocument(
                reason = "clave desconocida 'foo'",
            ),
        )

        assertContains(message, "clave desconocida 'foo'")
    }

    @Test
    fun `keeps the specific value of a negative blank line count`() {
        val message = reporter.describe(
            PrintScriptV1FormatterConfigurationError.NegativeBlankLineCount(providedValue = -3),
        )

        assertContains(message, "-3")
    }

    @Test
    fun `unwraps a V1 configuration failure inherited by V1_1`() {
        val message = reporter.describe(
            PrintScriptV11FormatterConfigurationError.V1ConfigurationFailure(
                error = PrintScriptV1FormatterConfigurationError.NegativeBlankLineCount(providedValue = -7),
            ),
        )

        assertContains(message, "-7")
    }

    @Test
    fun `keeps the specific value of a negative indentation size`() {
        val message = reporter.describe(
            PrintScriptV11FormatterConfigurationError.NegativeIndentationSize(providedValue = -2),
        )

        assertContains(message, "-2")
    }

    @Test
    fun `keeps the specific value of an unknown identifier format`() {
        val v1Message = reporter.describe(
            PrintScriptV1LinterConfigurationError.UnknownIdentifierFormat(providedValue = "kebab case"),
        )
        val v11Message = reporter.describe(
            PrintScriptV11LinterConfigurationError.UnknownIdentifierFormat(providedValue = "kebab case"),
        )

        assertContains(v1Message, "kebab case")
        assertContains(v11Message, "kebab case")
    }
}
