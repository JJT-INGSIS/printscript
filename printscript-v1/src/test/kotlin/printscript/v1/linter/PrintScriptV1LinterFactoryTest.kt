package printscript.v1.linter

import printscript.ast.DeclaredType
import printscript.ast.statement.VariableDeclarationStatement
import printscript.linter.Diagnostic
import printscript.linter.LintRule
import printscript.linter.RuleInspection
import printscript.linter.StatelessLintRule
import printscript.model.source.SourceSpan
import printscript.statement.Statement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class PrintScriptV1LinterFactoryTest {

    @Test
    fun `applies camel case and the println restriction by default`() {
        // given
        val linter = PrintScriptV1LinterFactory.create()

        // when
        val diagnostics = diagnosticsOf(
            linter,
            declare("my_total", DeclaredType.NUMBER, number("1")),
            printOf(sum(number("2"), number("3"))),
        )

        // then
        assertIs<PrintScriptV1Diagnostic.NamingConventionViolation>(diagnostics.first())
        assertIs<PrintScriptV1Diagnostic.UnsupportedPrintlnArgument>(diagnostics.last())
    }

    /**
     * Una regla ausente de la configuración no se construye, así que su
     * incumplimiento no se reporta.
     */
    @Test
    fun `ignores the rules left out of the configuration`() {
        // given
        val linter = linterWith(printlnArgumentRule())

        // when
        val diagnostics = diagnosticsOf(
            linter,
            declare("my_total", DeclaredType.NUMBER, number("1")),
        )

        // then
        diagnostics.assertNoDiagnostics()
    }

    @Test
    fun `reports nothing when the configuration has no rule`() {
        // given
        val linter = linterWith()

        // when
        val diagnostics = diagnosticsOf(
            linter,
            declare("my_total", DeclaredType.NUMBER, number("1")),
            printOf(sum(number("2"), number("3"))),
        )

        // then
        diagnostics.assertNoDiagnostics()
    }

    @Test
    fun `V1 rules ignore statements implemented by extensions`() {
        // given
        val linter = PrintScriptV1LinterFactory.create()

        // when
        val diagnostics = diagnosticsOf(linter, ExtensionStatement())

        // then
        diagnostics.assertNoDiagnostics()
    }

    /**
     * Una regla escrita afuera del proyecto corre junto a las de V1 sin
     * tocar ni el motor ni la versión del lenguaje.
     */
    @Test
    fun `runs a rule contributed from outside next to the V1 rules`() {
        // given
        val linter = PrintScriptV1LinterFactory.create(
            additionalRules = listOf(ForbiddenNameRule(forbiddenName = "temp")),
        )

        // when
        val diagnostics = diagnosticsOf(
            linter,
            declare("temp", DeclaredType.NUMBER, number("1")),
        )

        // then
        assertIs<ForbiddenNameDiagnostic>(diagnostics.single())
    }

    /**
     * El motor no sabe qué es una regla con memoria: el contrato alcanza
     * para escribir una desde afuera.
     */
    @Test
    fun `runs a remembering rule contributed from outside`() {
        // given
        val linter = PrintScriptV1LinterFactory.create(
            configuration = PrintScriptV1LinterConfiguration(rules = emptyList()),
            additionalRules = listOf(RedeclaredNameRule()),
        )

        // when
        val diagnostics = diagnosticsOf(
            linter,
            declare("total", DeclaredType.NUMBER, number("1")),
            declare("other", DeclaredType.NUMBER, number("2")),
            declare("total", DeclaredType.NUMBER, number("3")),
        )

        // then
        assertEquals(
            expected = 1,
            actual = diagnostics.size,
        )
    }
}

private data class ForbiddenNameDiagnostic(
    override val span: SourceSpan,
) : Diagnostic

private class ForbiddenNameRule(
    private val forbiddenName: String,
) : StatelessLintRule() {

    protected override fun diagnosticsIn(statement: Statement): List<Diagnostic> {
        return declaredNameOf(statement)
            ?.takeIf { declaredName -> declaredName == forbiddenName }
            ?.let { listOf(ForbiddenNameDiagnostic(statement.span)) }
            ?: emptyList()
    }
}

/**
 * Observa un nombre declarado dos veces: solo puede hacerlo acordándose
 * de las declaraciones anteriores.
 */
private class RedeclaredNameRule(
    private val declaredNames: Set<String> = emptySet(),
) : LintRule {

    override fun inspect(statement: Statement): RuleInspection {
        val declaredName = declaredNameOf(statement)

        return RuleInspection(
            diagnostics = redeclarationsOf(declaredName, statement),
            resultingRule = RedeclaredNameRule(
                declaredNames = declaredNames + setOfNotNull(declaredName),
            ),
        )
    }

    private fun redeclarationsOf(declaredName: String?, statement: Statement): List<Diagnostic> {
        return declaredName
            ?.takeIf { candidate -> candidate in declaredNames }
            ?.let { listOf(ForbiddenNameDiagnostic(statement.span)) }
            ?: emptyList()
    }
}

private fun declaredNameOf(statement: Statement): String? {
    return (statement as? VariableDeclarationStatement)
        ?.identifier
        ?.value
}
