package printscript.interpreter

import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan
import printscript.statement.ParseError
import printscript.statement.Statement
import printscript.statement.StatementReadResult
import printscript.statement.StatementSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotSame
import kotlin.test.assertSame

class InterpreterFactoryTest {

    @Test
    fun `executes external strategies while carrying their immutable state`() {
        val executor = AdvancingExecutor()
        val result = interpreterWith(executor).interpret(
            ListStatementSource(
                statements = listOf(
                    TestStatement("first"),
                    TestStatement("second"),
                ),
            ),
        )

        assertEquals(expected = InterpretationResult.Success, actual = result)
        assertEquals(
            expected = listOf(TestState(0), TestState(1)),
            actual = executor.receivedStates,
        )
    }

    @Test
    fun `uses the first executor that supports a statement`() {
        val first = AdvancingExecutor()
        val second = AdvancingExecutor()

        interpreterWith(first, second).interpret(
            ListStatementSource(listOf(TestStatement("value"))),
        )

        assertEquals(expected = 1, actual = first.executionCount)
        assertEquals(expected = 0, actual = second.executionCount)
    }

    @Test
    fun `copies executor configuration defensively`() {
        val executor = AdvancingExecutor()
        val executors = mutableListOf<StatementExecutor<TestState>>(executor)
        val interpreter = InterpreterFactory.create(
            initialState = TestState(0),
            statementExecutors = executors,
        )

        executors.clear()

        val result = interpreter.interpret(
            ListStatementSource(listOf(TestStatement("value"))),
        )

        assertEquals(expected = InterpretationResult.Success, actual = result)
        assertEquals(expected = 1, actual = executor.executionCount)
    }

    @Test
    fun `preserves errors returned by external executors`() {
        val expectedError = TestSemanticError(testSpan)
        val result = interpreterWith(FailingExecutor(expectedError)).interpret(
            ListStatementSource(listOf(TestStatement("value"))),
        )

        val failure = assertIs<InterpretationResult.SemanticFailure>(result)

        assertSame(expected = expectedError, actual = failure.error)
    }

    @Test
    fun `reports unsupported statements when no executor accepts them`() {
        val result = interpreterWith().interpret(
            ListStatementSource(listOf(TestStatement("value"))),
        )

        val failure = assertIs<InterpretationResult.SemanticFailure>(result)
        val error = assertIs<SemanticError.UnsupportedStatement>(failure.error)

        assertEquals(expected = testSpan, actual = error.span)
    }

    @Test
    fun `propagates parse failures without invoking executors`() {
        val executor = AdvancingExecutor()
        val expectedError = TestParseError(testSpan)
        val result = interpreterWith(executor).interpret(
            FailingStatementSource(expectedError),
        )

        val failure = assertIs<InterpretationResult.ParseFailure>(result)

        assertSame(expected = expectedError, actual = failure.error)
        assertEquals(expected = 0, actual = executor.executionCount)
    }

    @Test
    fun `stops pulling statements after an execution failure`() {
        val readCounter = ReadCounter()
        val result = interpreterWith(FailingExecutor(TestSemanticError(testSpan))).interpret(
            CountingStatementSource(
                statements = listOf(
                    TestStatement("failing"),
                    TestStatement("unread"),
                ),
                readCounter = readCounter,
            ),
        )

        assertIs<InterpretationResult.SemanticFailure>(result)
        assertEquals(expected = 1, actual = readCounter.count)
    }

    @Test
    fun `an empty source finishes successfully`() {
        val result = interpreterWith().interpret(
            ListStatementSource(emptyList()),
        )

        assertEquals(expected = InterpretationResult.Success, actual = result)
    }
}

class StatementExecutionContextTest {

    @Test
    fun `executes nested statements with the same configured engine`() {
        val first = AdvancingExecutor()
        val second = AdvancingExecutor()
        val result = interpreterWith(
            CompositeExecutor(),
            first,
            second,
        ).interpret(
            ListStatementSource(
                listOf(
                    CompositeTestStatement(
                        statements = listOf(
                            TestStatement("first"),
                            CompositeTestStatement(
                                statements = listOf(TestStatement("nested")),
                            ),
                            TestStatement("last"),
                        ),
                    ),
                    TestStatement("after composite"),
                ),
            ),
        )

        assertEquals(expected = InterpretationResult.Success, actual = result)
        assertEquals(
            expected = listOf(TestState(0), TestState(1), TestState(2), TestState(3)),
            actual = first.receivedStates,
        )
        assertEquals(expected = 0, actual = second.executionCount)
    }

    @Test
    fun `changing state creates a new context without changing the previous state`() {
        val executor = ContextReplacingExecutor()

        val result = interpreterWith(executor).interpret(
            ListStatementSource(listOf(TestStatement("value"))),
        )

        assertEquals(expected = InterpretationResult.Success, actual = result)
        assertEquals(
            expected = listOf(TestState(0), TestState(1)),
            actual = executor.receivedContexts.map { it.state },
        )
        assertNotSame(executor.receivedContexts.first(), executor.receivedContexts.last())
    }

    @Test
    fun `reports unsupported statements found during nested execution`() {
        val result = interpreterWith(CompositeExecutor()).interpret(
            ListStatementSource(
                listOf(
                    CompositeTestStatement(
                        statements = listOf(TestStatement("unsupported")),
                    ),
                ),
            ),
        )

        val failure = assertIs<InterpretationResult.SemanticFailure>(result)
        val error = assertIs<SemanticError.UnsupportedStatement>(failure.error)

        assertEquals(expected = testSpan, actual = error.span)
    }

    @Test
    fun `stops nested and top level execution after a nested failure`() {
        val readCounter = ReadCounter()
        val executor = FailingOnStatementExecutor(failingValue = "failing")
        val result = interpreterWith(CompositeExecutor(), executor).interpret(
            CountingStatementSource(
                statements = listOf(
                    CompositeTestStatement(
                        statements = listOf(
                            TestStatement("executed"),
                            TestStatement("failing"),
                            TestStatement("unread nested"),
                        ),
                    ),
                    TestStatement("unread top level"),
                ),
                readCounter = readCounter,
            ),
        )

        assertIs<InterpretationResult.SemanticFailure>(result)
        assertEquals(
            expected = listOf("executed", "failing"),
            actual = executor.receivedValues,
        )
        assertEquals(expected = 1, actual = readCounter.count)
    }
}

private fun interpreterWith(vararg executors: StatementExecutor<TestState>): Interpreter {
    return InterpreterFactory.create(
        initialState = TestState(0),
        statementExecutors = executors.toList(),
    )
}

private data class TestState(
    val executionCount: Int,
)

private data class TestStatement(
    val value: String,
    override val span: SourceSpan = testSpan,
) : Statement

private data class CompositeTestStatement(
    val statements: List<Statement>,
    override val span: SourceSpan = testSpan,
) : Statement

private class AdvancingExecutor : StatementExecutor<TestState> {

    val receivedStates = mutableListOf<TestState>()

    val executionCount: Int
        get() = receivedStates.size

    override fun supportsStatement(statement: Statement): Boolean {
        return statement is TestStatement
    }

    override fun executeStatement(
        statement: Statement,
        context: StatementExecutionContext<TestState>,
    ): ExecutionResult<TestState> {
        receivedStates.add(context.state)

        return ExecutionResult.Success(
            TestState(context.state.executionCount + 1),
        )
    }
}

private class CompositeExecutor : StatementExecutor<TestState> {

    override fun supportsStatement(statement: Statement): Boolean {
        return statement is CompositeTestStatement
    }

    override fun executeStatement(
        statement: Statement,
        context: StatementExecutionContext<TestState>,
    ): ExecutionResult<TestState> {
        val composite = assertIs<CompositeTestStatement>(statement)
        return context.executeStatements(composite.statements)
    }
}

private class ContextReplacingExecutor : StatementExecutor<TestState> {

    val receivedContexts = mutableListOf<StatementExecutionContext<TestState>>()

    override fun supportsStatement(statement: Statement): Boolean {
        return statement is TestStatement
    }

    override fun executeStatement(
        statement: Statement,
        context: StatementExecutionContext<TestState>,
    ): ExecutionResult<TestState> {
        val updatedContext = context.withState(
            TestState(context.state.executionCount + 1),
        )
        receivedContexts.add(context)
        receivedContexts.add(updatedContext)

        return ExecutionResult.Success(updatedContext.state)
    }
}

private class FailingExecutor(
    private val error: SemanticError,
) : StatementExecutor<TestState> {

    override fun supportsStatement(statement: Statement): Boolean {
        return statement is TestStatement
    }

    override fun executeStatement(
        statement: Statement,
        context: StatementExecutionContext<TestState>,
    ): ExecutionResult<TestState> {
        return ExecutionResult.Failure(error)
    }
}

private class FailingOnStatementExecutor(
    private val failingValue: String,
) : StatementExecutor<TestState> {

    val receivedValues = mutableListOf<String>()

    override fun supportsStatement(statement: Statement): Boolean {
        return statement is TestStatement
    }

    override fun executeStatement(
        statement: Statement,
        context: StatementExecutionContext<TestState>,
    ): ExecutionResult<TestState> {
        val testStatement = assertIs<TestStatement>(statement)
        receivedValues.add(testStatement.value)

        if (testStatement.value == failingValue) {
            return ExecutionResult.Failure(TestSemanticError(testStatement.span))
        }

        return ExecutionResult.Success(
            TestState(context.state.executionCount + 1),
        )
    }
}

private class ListStatementSource(
    private val statements: List<Statement>,
) : StatementSource {

    override fun nextStatement(): StatementReadResult {
        val statement = statements.firstOrNull()
            ?: return StatementReadResult.EndOfInput

        return StatementReadResult.Success(
            statement = statement,
            remainingSource = ListStatementSource(statements.drop(1)),
        )
    }
}

private class CountingStatementSource(
    private val statements: List<Statement>,
    private val readCounter: ReadCounter,
) : StatementSource {

    override fun nextStatement(): StatementReadResult {
        readCounter.count++

        val statement = statements.firstOrNull()
            ?: return StatementReadResult.EndOfInput

        return StatementReadResult.Success(
            statement = statement,
            remainingSource = CountingStatementSource(
                statements = statements.drop(1),
                readCounter = readCounter,
            ),
        )
    }
}

private class ReadCounter {
    var count: Int = 0
}

private class FailingStatementSource(
    private val error: ParseError,
) : StatementSource {

    override fun nextStatement(): StatementReadResult {
        return StatementReadResult.Failure(error)
    }
}

private data class TestSemanticError(
    override val span: SourceSpan,
) : SemanticError

private data class TestParseError(
    override val span: SourceSpan,
) : ParseError

private val testSpan = SourceSpan(
    start = SourcePosition(line = 1, column = 1, offset = 0),
    end = SourcePosition(line = 1, column = 1, offset = 0),
)
