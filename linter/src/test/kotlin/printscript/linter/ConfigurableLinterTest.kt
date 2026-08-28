package printscript.linter

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

private const val EXPECTED_READS_UNTIL_FIRST_DIAGNOSTIC = 2
private const val DIAGNOSTICS_PER_STATEMENT = 2

class ConfigurableLinterTest {

    @Test
    fun `reports end of input for an empty program`() {
        // given
        val linter = LinterFactory.create(rules = listOf(NameReportingRule("any", setOf("first"))))

        // when
        val readResult = linter.lint(statementsNamed()).nextDiagnostic()

        // then
        assertEquals(
            expected = DiagnosticReadResult.EndOfInput,
            actual = readResult,
        )
    }

    @Test
    fun `reports no diagnostics when no rule is configured`() {
        // given
        val linter = LinterFactory.create(rules = emptyList())

        // when
        val diagnostics = linter.lint(statementsNamed("first", "second")).readAll()

        // then
        assertEquals(
            expected = emptyList(),
            actual = diagnostics,
        )
    }

    /**
     * Fan-out: las reglas no compiten por una sentencia, se acumulan
     * sobre ella y se entrega la evidencia de todas.
     */
    @Test
    fun `runs every configured rule over the whole program`() {
        // given
        val linter = LinterFactory.create(
            rules = listOf(
                NameReportingRule(label = "naming", reportedNames = setOf("first", "second")),
                NameReportingRule(label = "argument", reportedNames = setOf("second")),
            ),
        )

        // when
        val diagnostics = linter.lint(statementsNamed("first", "second")).readAll()

        // then
        assertEquals(
            expected = listOf("naming", "naming", "argument"),
            actual = diagnostics.labels(),
        )
    }

    @Test
    fun `propagates a parse failure instead of a diagnostic`() {
        // given
        val error = unexpectedTokenError()
        val linter = LinterFactory.create(rules = listOf(NameReportingRule("any", setOf("first"))))

        // when
        val readResult = linter.lint(FailingStatementSource(error)).nextDiagnostic()

        // then
        val failure = assertIs<DiagnosticReadResult.Failure>(readResult)

        assertEquals(
            expected = error,
            actual = failure.error,
        )
    }

    /**
     * El linter tira del parser de a una sentencia: no lee el programa
     * entero para devolver el primer diagnóstico.
     */
    @Test
    fun `reads only the statements needed to reach the first diagnostic`() {
        // given
        val source = CountingStatementSource(statementsNamed("clean", "dirty", "dirty"))
        val linter = LinterFactory.create(rules = listOf(NameReportingRule("any", setOf("dirty"))))

        // when
        val readResult = linter.lint(source).nextDiagnostic()

        // then
        assertIs<DiagnosticReadResult.Success>(readResult)

        assertEquals(
            expected = EXPECTED_READS_UNTIL_FIRST_DIAGNOSTIC,
            actual = source.readCount,
        )
    }

    /**
     * Una sentencia puede incumplir varias reglas. La segunda infracción
     * ya está en la fuente: entregarla no vuelve a tocar el parser.
     */
    @Test
    fun `delivers every diagnostic of a statement before reading the next one`() {
        // given
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

        // when
        val first = assertIs<DiagnosticReadResult.Success>(
            linter.lint(source).nextDiagnostic(),
        )

        val readCountAfterFirst = source.readCount

        assertIs<DiagnosticReadResult.Success>(
            first.remainingSource.nextDiagnostic(),
        )

        // then
        assertEquals(
            expected = readCountAfterFirst,
            actual = source.readCount,
        )
    }

    /**
     * La razón de que una regla devuelva su sucesora: sin memoria entre
     * sentencias, esta regla no se puede escribir.
     */
    @Test
    fun `keeps what a rule remembered from earlier statements`() {
        // given
        val linter = LinterFactory.create(rules = listOf(RepeatedNameRule()))

        // when
        val diagnostics = linter
            .lint(statementsNamed("total", "count", "total", "count", "total"))
            .readAll()

        // then
        assertEquals(
            expected = listOf("total", "count", "total"),
            actual = diagnostics.labels(),
        )
    }

    /**
     * La memoria de una regla sobrevive a la entrega: recordar no depende
     * de cuántos diagnósticos quedaron pendientes.
     */
    @Test
    fun `keeps what a rule remembered across a composition`() {
        // given
        val linter = LinterFactory.create(
            rules = listOf(
                NameReportingRule(label = "naming", reportedNames = setOf("total")),
                RepeatedNameRule(),
            ),
        )

        // when
        val diagnostics = linter.lint(statementsNamed("total", "total")).readAll()

        // then
        assertEquals(
            expected = listOf("naming", "naming", "total"),
            actual = diagnostics.labels(),
        )
    }

    @Test
    fun `leaves a stateless rule unchanged after inspecting a statement`() {
        // given
        val rule = NameReportingRule(label = "any", reportedNames = setOf("first"))

        // when
        val inspection = rule.inspect(TestStatement("first"))

        // then
        assertEquals(
            expected = rule,
            actual = inspection.resultingRule,
        )
    }
}
