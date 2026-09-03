package printscript.linter

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

private const val EXPECTED_READS_UNTIL_FIRST_DIAGNOSTIC = 2
private const val DIAGNOSTICS_PER_STATEMENT = 2

class ConfigurableLinterTest {

    @Test
    fun `reports end of input for an empty program`() {
        val linter = LinterFactory.create(rules = listOf(NameReportingRule("any", setOf("first"))))

        val readResult = linter.lint(statementsNamed()).nextDiagnostic()

        assertEquals(
            expected = DiagnosticReadResult.EndOfInput,
            actual = readResult,
        )
    }

    @Test
    fun `reports no diagnostics when no rule is configured`() {
        val linter = LinterFactory.create(rules = emptyList())

        val diagnostics = linter.lint(statementsNamed("first", "second")).readAll()

        assertEquals(
            expected = emptyList(),
            actual = diagnostics,
        )
    }

    @Test
    fun `runs every configured rule over the whole program`() {
        val linter = LinterFactory.create(
            rules = listOf(
                NameReportingRule(label = "naming", reportedNames = setOf("first", "second")),
                NameReportingRule(label = "argument", reportedNames = setOf("second")),
            ),
        )

        val diagnostics = linter.lint(statementsNamed("first", "second")).readAll()

        assertEquals(
            expected = listOf("naming", "naming", "argument"),
            actual = diagnostics.labels(),
        )
    }

    @Test
    fun `propagates a parse failure instead of a diagnostic`() {
        val error = unexpectedTokenError()
        val linter = LinterFactory.create(rules = listOf(NameReportingRule("any", setOf("first"))))

        val readResult = linter.lint(FailingStatementSource(error)).nextDiagnostic()

        val failure = assertIs<DiagnosticReadResult.Failure>(readResult)

        assertEquals(
            expected = error,
            actual = failure.error,
        )
    }

    @Test
    fun `reads only the statements needed to reach the first diagnostic`() {
        val source = CountingStatementSource(statementsNamed("clean", "dirty", "dirty"))
        val linter = LinterFactory.create(rules = listOf(NameReportingRule("any", setOf("dirty"))))

        val readResult = linter.lint(source).nextDiagnostic()

        assertIs<DiagnosticReadResult.Success>(readResult)

        assertEquals(
            expected = EXPECTED_READS_UNTIL_FIRST_DIAGNOSTIC,
            actual = source.readCount,
        )
    }

    @Test
    fun `delivers every diagnostic of a statement before reading the next one`() {
        val source = CountingStatementSource(statementsNamed("dirty", "dirty"))
        val linter = LinterFactory.create(
            rules = listOf(
                NameReportingRule(
                    label = "any",
                    reportedNames = setOf("dirty"),
                    reportCount = DIAGNOSTICS_PER_STATEMENT,
                ),
            ),
        )

        val first = assertIs<DiagnosticReadResult.Success>(
            linter.lint(source).nextDiagnostic(),
        )

        val readCountAfterFirst = source.readCount

        assertIs<DiagnosticReadResult.Success>(
            first.remainingSource.nextDiagnostic(),
        )

        assertEquals(
            expected = readCountAfterFirst,
            actual = source.readCount,
        )
    }

    @Test
    fun `keeps what a rule remembered from earlier statements`() {
        val linter = LinterFactory.create(rules = listOf(RepeatedNameRule()))

        val diagnostics = linter
            .lint(statementsNamed("total", "count", "total", "count", "total"))
            .readAll()

        assertEquals(
            expected = listOf("total", "count", "total"),
            actual = diagnostics.labels(),
        )
    }

    @Test
    fun `keeps what a rule remembered across a composition`() {
        val linter = LinterFactory.create(
            rules = listOf(
                NameReportingRule(label = "naming", reportedNames = setOf("total")),
                RepeatedNameRule(),
            ),
        )

        val diagnostics = linter.lint(statementsNamed("total", "total")).readAll()

        assertEquals(
            expected = listOf("naming", "naming", "total"),
            actual = diagnostics.labels(),
        )
    }

    @Test
    fun `leaves a stateless rule unchanged after inspecting a statement`() {
        val rule = NameReportingRule(label = "any", reportedNames = setOf("first"))

        val inspection = rule.inspect(TestStatement("first"))

        assertEquals(
            expected = rule,
            actual = inspection.resultingRule,
        )
    }
}
