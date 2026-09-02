package printscript.formatter

import printscript.statement.Statement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame

class FormatterFactoryTest {

    @Test
    fun `does not read statements until formatted output is requested`() {
        val statementSource = CountingStatementSource(
            ListStatementSource(
                statements = listOf(
                    TestStatement("value"),
                    TestStatement("unread"),
                ),
            ),
        )
        val formattedSource = formatterWith(
            statementFormatters = listOf(SuccessfulTestStatementFormatter("")),
        ).format(statementSource)

        assertEquals(expected = 0, actual = statementSource.readCount)

        formattedSource.nextFormattedStatement()

        assertEquals(expected = 1, actual = statementSource.readCount)
    }

    @Test
    fun `uses first formatter that supports a statement`() {
        val result = formatOne(
            formatter = formatterWith(
                statementFormatters = listOf(
                    SuccessfulTestStatementFormatter("first:"),
                    SuccessfulTestStatementFormatter("second:"),
                ),
            ),
            statement = TestStatement("value"),
        )

        assertEquals(expected = "first:value", actual = result.formattedText)
    }

    @Test
    fun `formats nested statements with the same configured engine`() {
        val result = formatOne(
            formatter = formatterWith(
                statementFormatters = listOf(
                    CompositeTestStatementFormatter,
                    SuccessfulTestStatementFormatter(""),
                ),
                statementSeparationPolicy = PipeSeparationPolicy,
            ),
            statement = CompositeTestStatement(
                statements = listOf(
                    TestStatement("first"),
                    CompositeTestStatement(
                        statements = listOf(TestStatement("nested")),
                    ),
                    TestStatement("last"),
                ),
            ),
        )

        assertEquals(expected = "[first|[nested]|last]", actual = result.formattedText)
    }

    @Test
    fun `nested statements preserve formatter priority`() {
        val result = formatOne(
            formatter = formatterWith(
                statementFormatters = listOf(
                    CompositeTestStatementFormatter,
                    SuccessfulTestStatementFormatter("first:"),
                    SuccessfulTestStatementFormatter("second:"),
                ),
            ),
            statement = CompositeTestStatement(
                statements = listOf(TestStatement("value")),
            ),
        )

        assertEquals(expected = "[first:value]", actual = result.formattedText)
    }

    @Test
    fun `nested formatting stops and propagates the first failure`() {
        val expectedError = TestFormattingError()
        val recordingFormatter = RecordingTestStatementFormatter(
            failureByValue = mapOf("failure" to expectedError),
        )
        val result = formatterWith(
            statementFormatters = listOf(
                CompositeTestStatementFormatter,
                recordingFormatter,
            ),
        ).format(
            ListStatementSource(
                statements = listOf(
                    CompositeTestStatement(
                        statements = listOf(
                            TestStatement("first"),
                            TestStatement("failure"),
                            TestStatement("unread"),
                        ),
                    ),
                ),
            ),
        ).nextFormattedStatement()

        val failure = assertIs<FormattedStatementReadResult.Failure>(result)

        assertSame(expected = expectedError, actual = failure.error)
        assertEquals(expected = listOf("first", "failure"), actual = recordingFormatter.formattedValues)
    }

    @Test
    fun `reports unsupported statements found inside a composite statement`() {
        val result = formatterWith(
            statementFormatters = listOf(CompositeTestStatementFormatter),
        ).format(
            ListStatementSource(
                statements = listOf(
                    CompositeTestStatement(
                        statements = listOf(TestStatement("unsupported")),
                    ),
                ),
            ),
        ).nextFormattedStatement()

        val failure = assertIs<FormattedStatementReadResult.Failure>(result)

        assertIs<FormattingError.UnsupportedStatement>(failure.error)
    }

    @Test
    fun `formats an empty nested statement list as empty text`() {
        val result = formatOne(
            formatter = formatterWith(
                statementFormatters = listOf(CompositeTestStatementFormatter),
            ),
            statement = CompositeTestStatement(statements = emptyList()),
        )

        assertEquals(expected = "[]", actual = result.formattedText)
    }

    @Test
    fun `formatter configuration cannot change after creation`() {
        val statementFormatters = mutableListOf<StatementFormatter>(
            SuccessfulTestStatementFormatter("stable:"),
        )
        val formatter = formatterWith(statementFormatters)

        statementFormatters.clear()

        val result = formatOne(
            formatter = formatter,
            statement = TestStatement("value"),
        )

        assertEquals(expected = "stable:value", actual = result.formattedText)
    }

    @Test
    fun `applies separation policy before every statement`() {
        val formattedSource = formatterWith(
            statementFormatters = listOf(SuccessfulTestStatementFormatter("")),
            statementSeparationPolicy = TestSeparationPolicy,
        ).format(
            ListStatementSource(
                statements = listOf(
                    TestStatement("first"),
                    TestStatement("second"),
                ),
            ),
        )

        val first = assertIs<FormattedStatementReadResult.Success>(
            formattedSource.nextFormattedStatement(),
        )
        val second = assertIs<FormattedStatementReadResult.Success>(
            first.remainingSource.nextFormattedStatement(),
        )

        assertEquals(expected = "<first>falsefirst", actual = first.formattedText)
        assertEquals(expected = "<second>truesecond", actual = second.formattedText)
    }

    @Test
    fun `preserves errors returned by external formatters`() {
        val expectedError = TestFormattingError()
        val formatter = formatterWith(
            statementFormatters = listOf(FailingTestStatementFormatter(expectedError)),
        )
        val result = formatter.format(
            ListStatementSource(
                statements = listOf(TestStatement("value")),
            ),
        ).nextFormattedStatement()

        val failure = assertIs<FormattedStatementReadResult.Failure>(result)

        assertSame(expected = expectedError, actual = failure.error)
    }

    @Test
    fun `reports unsupported statements when no formatter accepts them`() {
        val formatter = formatterWith(statementFormatters = emptyList())
        val result = formatter.format(
            ListStatementSource(
                statements = listOf(TestStatement("value")),
            ),
        ).nextFormattedStatement()

        val failure = assertIs<FormattedStatementReadResult.Failure>(result)

        assertIs<FormattingError.UnsupportedStatement>(failure.error)
    }

    @Test
    fun `propagates parse failures from statement source`() {
        val expectedError = TestParseError()
        val result = formatterWith(emptyList()).format(
            FailingStatementSource(expectedError),
        ).nextFormattedStatement()

        val formattingError = assertIs<FormattingError.ParseFailure>(
            assertIs<FormattedStatementReadResult.Failure>(result).error,
        )

        assertSame(expected = expectedError, actual = formattingError.parseError)
    }

    @Test
    fun `reports end of input`() {
        val result = formatterWith(emptyList()).format(
            ListStatementSource(statements = emptyList()),
        ).nextFormattedStatement()

        assertIs<FormattedStatementReadResult.EndOfInput>(result)
    }

    private fun formatterWith(
        statementFormatters: List<StatementFormatter>,
        statementSeparationPolicy: StatementSeparationPolicy = EmptySeparationPolicy,
    ): Formatter {
        return FormatterFactory.create(
            statementFormatters = statementFormatters,
            statementSeparationPolicy = statementSeparationPolicy,
        )
    }

    private fun formatOne(formatter: Formatter, statement: Statement): FormattedStatementReadResult.Success {
        return assertIs<FormattedStatementReadResult.Success>(
            formatter.format(
                ListStatementSource(
                    statements = listOf(statement),
                ),
            ).nextFormattedStatement(),
        )
    }
}

private class SuccessfulTestStatementFormatter(
    private val prefix: String,
) : StatementFormatter {

    override fun supportsStatement(statement: Statement): Boolean {
        return statement is TestStatement
    }

    override fun formatStatement(
        statement: Statement,
        context: StatementFormattingContext,
    ): StatementFormattingResult {
        val testStatement = assertIs<TestStatement>(statement)

        return StatementFormattingResult.Success(
            formattedText = prefix + testStatement.value,
        )
    }
}

private class FailingTestStatementFormatter(
    private val error: FormattingError,
) : StatementFormatter {

    override fun supportsStatement(statement: Statement): Boolean {
        return statement is TestStatement
    }

    override fun formatStatement(
        statement: Statement,
        context: StatementFormattingContext,
    ): StatementFormattingResult {
        return StatementFormattingResult.Failure(error)
    }
}

private data object CompositeTestStatementFormatter : StatementFormatter {

    override fun supportsStatement(statement: Statement): Boolean {
        return statement is CompositeTestStatement
    }

    override fun formatStatement(
        statement: Statement,
        context: StatementFormattingContext,
    ): StatementFormattingResult {
        val compositeStatement = assertIs<CompositeTestStatement>(statement)

        return when (val result = context.formatStatements(compositeStatement.statements)) {
            is StatementFormattingResult.Failure -> result
            is StatementFormattingResult.Success ->
                StatementFormattingResult.Success(
                    formattedText = "[${result.formattedText}]",
                )
        }
    }
}

private class RecordingTestStatementFormatter(
    private val failureByValue: Map<String, FormattingError>,
) : StatementFormatter {

    val formattedValues: MutableList<String> = mutableListOf()

    override fun supportsStatement(statement: Statement): Boolean {
        return statement is TestStatement
    }

    override fun formatStatement(
        statement: Statement,
        context: StatementFormattingContext,
    ): StatementFormattingResult {
        val testStatement = assertIs<TestStatement>(statement)
        formattedValues.add(testStatement.value)

        val failure = failureByValue[testStatement.value]
        if (failure != null) {
            return StatementFormattingResult.Failure(failure)
        }

        return StatementFormattingResult.Success(testStatement.value)
    }
}

private data object EmptySeparationPolicy : StatementSeparationPolicy {

    override fun separatorBeforeStatement(statement: Statement, hasPreviousStatement: Boolean): String {
        return ""
    }
}

private data object TestSeparationPolicy : StatementSeparationPolicy {

    override fun separatorBeforeStatement(statement: Statement, hasPreviousStatement: Boolean): String {
        val testStatement = assertIs<TestStatement>(statement)

        return "<${testStatement.value}>$hasPreviousStatement"
    }
}

private data object PipeSeparationPolicy : StatementSeparationPolicy {

    override fun separatorBeforeStatement(statement: Statement, hasPreviousStatement: Boolean): String {
        return if (hasPreviousStatement) "|" else ""
    }
}
