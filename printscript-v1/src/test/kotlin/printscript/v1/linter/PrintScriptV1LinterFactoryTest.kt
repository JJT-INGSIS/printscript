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
        val linter = PrintScriptV1LinterFactory.create()

        val diagnostics = diagnosticsOf(
            linter,
            declare("my_total", DeclaredType.NUMBER, number("1")),
            printOf(sum(number("2"), number("3"))),
        )

        assertIs<PrintScriptV1Diagnostic.NamingConventionViolation>(diagnostics.first())
        assertIs<PrintScriptV1Diagnostic.UnsupportedPrintlnArgument>(diagnostics.last())
    }

    @Test
    fun `ignores the rules left out of the configuration`() {
        val linter = linterWith(printlnArgumentRule())

        val diagnostics = diagnosticsOf(
            linter,
            declare("my_total", DeclaredType.NUMBER, number("1")),
        )

        diagnostics.assertNoDiagnostics()
    }

    @Test
    fun `reports nothing when the configuration has no rule`() {
        val linter = linterWith()

        val diagnostics = diagnosticsOf(
            linter,
            declare("my_total", DeclaredType.NUMBER, number("1")),
            printOf(sum(number("2"), number("3"))),
        )

        diagnostics.assertNoDiagnostics()
    }

    @Test
    fun `V1 rules ignore statements implemented by extensions`() {
        val linter = PrintScriptV1LinterFactory.create()

        val diagnostics = diagnosticsOf(linter, ExtensionStatement())

        diagnostics.assertNoDiagnostics()
    }

    @Test
    fun `runs a rule contributed from outside next to the V1 rules`() {
        val linter = PrintScriptV1LinterFactory.create(
            additionalRules = listOf(ForbiddenNameRule(forbiddenName = "temp")),
        )

        val diagnostics = diagnosticsOf(
            linter,
            declare("temp", DeclaredType.NUMBER, number("1")),
        )

        assertIs<ForbiddenNameDiagnostic>(diagnostics.single())
    }

    @Test
    fun `runs a remembering rule contributed from outside`() {
        val linter = PrintScriptV1LinterFactory.create(
            configuration = PrintScriptV1LinterConfiguration(rules = emptyList()),
            additionalRules = listOf(RedeclaredNameRule()),
        )

        val diagnostics = diagnosticsOf(
            linter,
            declare("total", DeclaredType.NUMBER, number("1")),
            declare("other", DeclaredType.NUMBER, number("2")),
            declare("total", DeclaredType.NUMBER, number("3")),
        )

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
