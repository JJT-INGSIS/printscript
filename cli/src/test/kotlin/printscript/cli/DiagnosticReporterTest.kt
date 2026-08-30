package printscript.cli

import printscript.ast.Identifier
import printscript.ast.expression.NumberLiteralExpression
import printscript.cli.internal.report.DiagnosticReporter
import printscript.linter.Diagnostic
import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan
import printscript.v1.linter.PrintScriptV1Diagnostic
import printscript.v1.linter.PrintScriptV1NamingConvention
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertContains

class DiagnosticReporterTest {

    private val reporter = DiagnosticReporter()

    private val anySpan = SourceSpan(
        start = SourcePosition(line = 3, column = 5, offset = 20),
        end = SourcePosition(line = 3, column = 9, offset = 24),
    )

    @Test
    fun `describes a V1 naming convention violation`() {
        val diagnostic = PrintScriptV1Diagnostic.NamingConventionViolation(
            identifier = Identifier(value = "invalid_name", span = anySpan),
            expectedConvention = PrintScriptV1NamingConvention.CAMEL_CASE,
        )

        val message = reporter.describe(diagnostic)

        assertContains(message, "'invalid_name'")
        assertContains(message, "camelCase")
        assertContains(message, "línea 3, columnas 5 a 9")
    }

    @Test
    fun `describes a V1 unsupported println argument`() {
        val diagnostic = PrintScriptV1Diagnostic.UnsupportedPrintlnArgument(
            argument = NumberLiteralExpression(
                value = BigDecimal.ONE,
                span = anySpan,
            ),
        )

        val message = reporter.describe(diagnostic)

        assertContains(message, "println no acepta una expresión como argumento")
        assertContains(message, "línea 3, columnas 5 a 9")
    }

    @Test
    fun `describes diagnostics implemented by extensions`() {
        val message = reporter.describe(ExtensionDiagnostic(anySpan))

        assertContains(message, "diagnóstico desconocido")
        assertContains(message, "línea 3, columnas 5 a 9")
    }

    private data class ExtensionDiagnostic(
        override val span: SourceSpan,
    ) : Diagnostic
}
