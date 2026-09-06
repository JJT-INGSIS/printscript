package printscript.linter

import kotlin.test.Test
import kotlin.test.assertEquals

class RuleInspectionTest {

    @Test
    fun `is unaffected by mutating the list passed to its constructor`() {
        val original = mutableListOf<Diagnostic>(TestDiagnostic(label = "first"))
        val rule = NameReportingRule(label = "any", reportedNames = emptySet())

        val inspection = RuleInspection(diagnostics = original, resultingRule = rule)
        original.add(TestDiagnostic(label = "second"))

        assertEquals(expected = 1, actual = inspection.diagnostics.size)
    }
}
