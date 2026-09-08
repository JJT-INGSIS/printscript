package printscript.v1.linter.rule

import printscript.ast.DeclaredType
import printscript.linter.Diagnostic
import printscript.linter.LintRule
import printscript.linter.RuleInspection
import printscript.model.source.SourceSpan
import printscript.statement.Statement
import printscript.v1.linter.PrintScriptV1Diagnostic
import printscript.v1.linter.assertNamingViolations
import printscript.v1.linter.assertNoDiagnostics
import printscript.v1.linter.blockOf
import printscript.v1.linter.declare
import printscript.v1.linter.diagnosticsOf
import printscript.v1.linter.grouped
import printscript.v1.linter.identifierNamingRule
import printscript.v1.linter.ifStatement
import printscript.v1.linter.number
import printscript.v1.linter.printOf
import printscript.v1.linter.printlnArgumentRule
import printscript.v1.linter.v11LinterWith
import printscript.v1.linter.variable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class PrintScriptV11StatementTreeRuleTest {

    @Test
    fun `applies configured rules to statements inside a then block`() {
        val linter = v11LinterWith(rules = listOf(identifierNamingRule()))

        val diagnostics = diagnosticsOf(
            linter,
            ifStatement(
                thenBranch = blockOf(declare("my_total", DeclaredType.NUMBER, number("1"))),
            ),
        )

        diagnostics.assertNamingViolations("my_total")
    }

    @Test
    fun `walks both then and else blocks in order`() {
        val linter = v11LinterWith(rules = listOf(identifierNamingRule()))

        val diagnostics = diagnosticsOf(
            linter,
            ifStatement(
                thenBranch = blockOf(declare("my_total", DeclaredType.NUMBER, number("1"))),
                elseBranch = blockOf(declare("my_count", DeclaredType.NUMBER, number("2"))),
            ),
        )

        diagnostics.assertNamingViolations("my_total", "my_count")
    }

    @Test
    fun `applies the println argument rule inside a nested block`() {
        val linter = v11LinterWith(rules = listOf(printlnArgumentRule()))

        val diagnostics = diagnosticsOf(
            linter,
            ifStatement(
                thenBranch = blockOf(printOf(grouped(variable("total")))),
            ),
        )

        assertIs<PrintScriptV1Diagnostic.UnsupportedPrintlnArgument>(diagnostics.single())
    }

    @Test
    fun `respects rules contributed from outside inside a nested block`() {
        val linter = v11LinterWith(
            additionalRules = listOf(ForbiddenNameRule(forbiddenName = "temp")),
        )

        val diagnostics = diagnosticsOf(
            linter,
            ifStatement(
                thenBranch = blockOf(declare("temp", DeclaredType.NUMBER, number("1"))),
            ),
        )

        assertIs<ForbiddenNameDiagnostic>(diagnostics.single())
    }

    @Test
    fun `does not diagnose an empty block`() {
        val linter = v11LinterWith(rules = listOf(identifierNamingRule()))

        val diagnostics = diagnosticsOf(linter, ifStatement(thenBranch = blockOf()))

        diagnostics.assertNoDiagnostics()
    }

    @Test
    fun `propagates state remembered by a rule across nested statements`() {
        val linter = v11LinterWith(additionalRules = listOf(RedeclaredNameRule()))

        val diagnostics = diagnosticsOf(
            linter,
            declare("total", DeclaredType.NUMBER, number("1")),
            ifStatement(
                thenBranch = blockOf(declare("total", DeclaredType.NUMBER, number("2"))),
            ),
        )

        assertEquals(expected = 1, actual = diagnostics.size)
    }
}

private data class ForbiddenNameDiagnostic(
    override val span: SourceSpan,
) : Diagnostic

private class ForbiddenNameRule(
    private val forbiddenName: String,
) : LintRule {

    override fun inspect(statement: Statement): RuleInspection {
        return RuleInspection(
            diagnostics = declaredNameOf(statement)
                ?.takeIf { declaredName -> declaredName == forbiddenName }
                ?.let { listOf(ForbiddenNameDiagnostic(statement.span)) }
                ?: emptyList(),
            resultingRule = this,
        )
    }
}

private class RedeclaredNameRule(
    private val declaredNames: Set<String> = emptySet(),
) : LintRule {

    override fun inspect(statement: Statement): RuleInspection {
        val declaredName = declaredNameOf(statement)

        return RuleInspection(
            diagnostics = declaredName
                ?.takeIf { candidate -> candidate in declaredNames }
                ?.let { listOf(ForbiddenNameDiagnostic(statement.span)) }
                ?: emptyList(),
            resultingRule = RedeclaredNameRule(
                declaredNames = declaredNames + setOfNotNull(declaredName),
            ),
        )
    }
}

private fun declaredNameOf(statement: Statement): String? {
    return (statement as? printscript.ast.statement.VariableDeclarationStatement)
        ?.identifier
        ?.value
}
