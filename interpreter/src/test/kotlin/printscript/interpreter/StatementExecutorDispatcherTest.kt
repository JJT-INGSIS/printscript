package printscript.interpreter

import printscript.interpreter.environment.Environment
import printscript.interpreter.statements.AssignmentExecutor
import printscript.interpreter.statements.DeclarationExecutor
import printscript.interpreter.statements.PrintlnExecutor
import printscript.interpreter.statements.StatementExecutor
import printscript.interpreter.statements.StatementExecutorDispatcher
import printscript.interpreter.value.RuntimeValue
import printscript.model.ast.DeclaredType
import printscript.model.ast.Identifier
import printscript.model.ast.expression.Expression
import printscript.model.ast.expression.NumberLiteralExpression
import printscript.model.ast.statement.AssignmentStatement
import printscript.model.ast.statement.PrintlnStatement
import printscript.model.ast.statement.Statement
import printscript.model.ast.statement.VariableDeclarationStatement
import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail

class StatementExecutorDispatcherTest {

    private val anySpan = SourceSpan(
        start = SourcePosition(1, 1, 0),
        end = SourcePosition(1, 1, 0),
    )

    private val numberExpression =
        NumberLiteralExpression(
            value = BigDecimal.ONE,
            span = anySpan,
        )

    private val declarationStatement =
        VariableDeclarationStatement(
            identifier = Identifier(
                value = "x",
                span = anySpan,
            ),
            declaredType = DeclaredType.NUMBER,
            initializer = numberExpression,
            span = anySpan,
        )

    private val assignmentStatement =
        AssignmentStatement(
            target = Identifier(
                value = "x",
                span = anySpan,
            ),
            expression = numberExpression,
            span = anySpan,
        )

    private val printlnStatement =
        PrintlnStatement(
            argument = numberExpression,
            span = anySpan,
        )

    @Test
    fun `dispatcher executes the first supporting executor`() {
        val nonMatchingExecutor =
            RecordingExecutor(
                supportsStatement = false,
            )

        val selectedExecutor =
            RecordingExecutor(
                supportsStatement = true,
            )

        val executorAfterMatch =
            RecordingExecutor(
                supportsStatement = true,
            )

        val dispatcher = StatementExecutorDispatcher(
            executors = listOf(
                nonMatchingExecutor,
                selectedExecutor,
                executorAfterMatch,
            ),
        )

        val result = dispatcher.execute(
            statement = printlnStatement,
            context = executionContext(),
        )

        assertEquals(
            expected = ExecutionResult.Success(Unit),
            actual = result,
        )

        assertEquals(
            expected = 0,
            actual = nonMatchingExecutor.executionCount,
        )

        assertEquals(
            expected = 1,
            actual = selectedExecutor.executionCount,
        )

        assertEquals(
            expected = 0,
            actual = executorAfterMatch.executionCount,
        )
    }

    @Test
    fun `dispatcher propagates executor failure`() {
        val expectedFailure: ExecutionResult<Unit> =
            ExecutionResult.Failure(
                SemanticError.DivisionByZero(anySpan),
            )

        val executor = RecordingExecutor(
            supportsStatement = true,
            result = expectedFailure,
        )

        val dispatcher = StatementExecutorDispatcher(
            executors = listOf(executor),
        )

        val result = dispatcher.execute(
            statement = printlnStatement,
            context = executionContext(),
        )

        assertEquals(
            expected = expectedFailure,
            actual = result,
        )

        assertEquals(
            expected = 1,
            actual = executor.executionCount,
        )
    }

    @Test
    fun `dispatcher returns unsupported statement when no executor matches`() {
        val dispatcher = StatementExecutorDispatcher(
            executors = emptyList(),
        )

        val result = dispatcher.execute(
            statement = printlnStatement,
            context = executionContext(),
        )

        assertEquals(
            expected = ExecutionResult.Failure(
                SemanticError.UnsupportedStatement(
                    span = anySpan,
                ),
            ),
            actual = result,
        )
    }

    @Test
    fun `concrete executors support only their statement type`() {
        val declarationExecutor = DeclarationExecutor()
        val assignmentExecutor = AssignmentExecutor()
        val printlnExecutor = PrintlnExecutor()

        assertTrue(
            declarationExecutor.supports(declarationStatement),
        )
        assertFalse(
            declarationExecutor.supports(assignmentStatement),
        )
        assertFalse(
            declarationExecutor.supports(printlnStatement),
        )

        assertTrue(
            assignmentExecutor.supports(assignmentStatement),
        )
        assertFalse(
            assignmentExecutor.supports(declarationStatement),
        )
        assertFalse(
            assignmentExecutor.supports(printlnStatement),
        )

        assertTrue(
            printlnExecutor.supports(printlnStatement),
        )
        assertFalse(
            printlnExecutor.supports(declarationStatement),
        )
        assertFalse(
            printlnExecutor.supports(assignmentStatement),
        )
    }

    private fun executionContext(): ExecutionContext {
        return UnusedExecutionContext
    }

    private class RecordingExecutor(
        private val supportsStatement: Boolean,
        private val result: ExecutionResult<Unit> =
            ExecutionResult.Success(Unit),
    ) : StatementExecutor {

        var executionCount: Int = 0
            private set

        override fun supports(
            statement: Statement,
        ): Boolean {
            return supportsStatement
        }

        override fun execute(
            statement: Statement,
            context: ExecutionContext,
        ): ExecutionResult<Unit> {
            executionCount++

            return result
        }
    }

    private object UnusedExecutionContext : ExecutionContext {

        override val environment: Environment
            get() = fail(
                "Dispatcher test must not access the environment",
            )

        override fun evaluate(
            expression: Expression,
        ): ExecutionResult<RuntimeValue> {
            fail(
                "Dispatcher test must not evaluate expressions",
            )
        }

        override fun emit(
            line: String,
        ) {
            fail(
                "Dispatcher test must not emit output",
            )
        }
    }
}