package printscript.linter

import printscript.ast.DeclaredType
import printscript.ast.expression.Expression
import printscript.ast.expression.StringLiteralExpression
import printscript.ast.expression.StringQuoteStyle
import printscript.ast.statement.Statement
import printscript.linter.internal.DiagnosticSearch
import printscript.linter.internal.PrintScriptLinter
import printscript.linter.internal.rule.CompositeRule
import printscript.linter.internal.rule.LintRule
import printscript.model.source.SourceSpan
import printscript.statement.ParseError
import printscript.token.Token
import printscript.v1.token.PrintScriptV1TokenType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

private const val EXPECTED_READS_UNTIL_FIRST_DIAGNOSTIC = 2
private const val EXPECTED_READS_WITHOUT_ADVANCING = 2

class PrintScriptLinterTest {

    private val everyRule: Linter = linterWith(
        RuleConfiguration.IdentifierNaming(NamingConvention.CAMEL_CASE),
        printlnArgumentRule(),
    )

    @Test
    fun `reports end of input for an empty program`() {
        // given
        val source = ListStatementSource(emptyList())

        // when
        val readResult = everyRule.lint(source).nextDiagnostic()

        // then
        assertEquals(
            expected = DiagnosticReadResult.EndOfInput,
            actual = readResult,
        )
    }

    @Test
    fun `reports no diagnostics for a compliant program`() {
        // given
        val declaration = declare("myTotal", DeclaredType.NUMBER, number("1"))
        val print = printOf(variable("myTotal"))

        // when
        val diagnostics = diagnosticsOf(everyRule, declaration, print)

        // then
        diagnostics.assertNoDiagnostics()
    }

    /**
     * Cada regla mira cada sentencia: las dos tienen que reportar sobre
     * el mismo programa.
     */
    @Test
    fun `runs every configured rule over the whole program`() {
        // given
        val declaration = declare("my_total", DeclaredType.NUMBER, number("1"))
        val print = printOf(sum(number("2"), number("3")))

        // when
        val diagnostics = diagnosticsOf(everyRule, declaration, print)

        // then
        assertIs<Diagnostic.NamingConventionViolation>(diagnostics.first())
        assertIs<Diagnostic.UnsupportedPrintlnArgument>(diagnostics.last())
    }

    /**
     * Una regla ausente de la configuración no se construye, así que su
     * incumplimiento no se reporta.
     */
    @Test
    fun `ignores the rules left out of the configuration`() {
        // given
        val linter = linterWith(printlnArgumentRule())
        val declaration = declare("my_total", DeclaredType.NUMBER, number("1"))

        // when
        val diagnostics = diagnosticsOf(linter, declaration)

        // then
        diagnostics.assertNoDiagnostics()
    }

    @Test
    fun `reports no diagnostics when no rule is configured`() {
        // given
        val linter = linterWith()
        val declaration = declare("my_total", DeclaredType.NUMBER, number("1"))
        val print = printOf(sum(number("2"), number("3")))

        // when
        val diagnostics = diagnosticsOf(linter, declaration, print)

        // then
        diagnostics.assertNoDiagnostics()
    }

    @Test
    fun `V1 rules ignore statements implemented by extensions`() {
        val diagnostics = diagnosticsOf(
            everyRule,
            ExtensionStatement(anySpan),
        )

        diagnostics.assertNoDiagnostics()
    }

    @Test
    fun `propagates a parse failure instead of a diagnostic`() {
        // given
        val error = unexpectedTokenError()
        val source = FailingStatementSource(error)

        // when
        val readResult = everyRule.lint(source).nextDiagnostic()

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
        val source = CountingStatementSource(
            ListStatementSource(
                listOf(
                    declare("myTotal", DeclaredType.NUMBER, number("1")),
                    declare("my_total", DeclaredType.NUMBER, number("2")),
                    declare("other_total", DeclaredType.NUMBER, number("3")),
                ),
            ),
        )

        // when
        val readResult = everyRule.lint(source).nextDiagnostic()

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
        val statement = printOf(number("1"))
        val source = CountingStatementSource(
            ListStatementSource(
                listOf(
                    statement,
                    printOf(number("2")),
                ),
            ),
        )

        val linter = PrintScriptLinter(
            search = DiagnosticSearch(
                rule = CompositeRule(
                    rules = listOf(
                        AlwaysReportingRule(
                            reportCount = EXPECTED_READS_WITHOUT_ADVANCING,
                        ),
                    ),
                ),
            ),
        )

        // when
        val first = assertIs<DiagnosticReadResult.Success>(
            linter.lint(source).nextDiagnostic(),
        )

        val readCountAfterFirst = source.readCount

        val second = assertIs<DiagnosticReadResult.Success>(
            first.remainingSource.nextDiagnostic(),
        )

        // then
        assertEquals(
            expected = statement.span,
            actual = second.diagnostic.span,
        )

        assertEquals(
            expected = readCountAfterFirst,
            actual = source.readCount,
        )
    }

    private fun unexpectedTokenError(): ParseError {
        return ParseError.UnexpectedToken(
            expected = setOf(PrintScriptV1TokenType.SEMICOLON),
            actual = Token(
                type = PrintScriptV1TokenType.EOF,
                lexeme = "",
                span = anySpan,
            ),
        )
    }
}

/**
 * Regla que reporta varias veces sobre la misma sentencia, para ejercitar
 * la cola de diagnósticos pendientes.
 */
private class AlwaysReportingRule(
    private val reportCount: Int,
) : LintRule {

    override fun inspect(statement: Statement): List<Diagnostic> {
        return List(reportCount) {
            Diagnostic.UnsupportedPrintlnArgument(
                argument = anyExpressionAt(statement.span),
            )
        }
    }

    private fun anyExpressionAt(span: SourceSpan): Expression {
        return StringLiteralExpression(
            value = "",
            quoteStyle = StringQuoteStyle.DOUBLE,
            span = span,
        )
    }
}

private data class ExtensionStatement(
    override val span: SourceSpan,
) : Statement
