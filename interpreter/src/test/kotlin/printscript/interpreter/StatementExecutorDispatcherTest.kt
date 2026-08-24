package printscript.interpreter

import printscript.ast.DeclaredType
import printscript.ast.Identifier
import printscript.ast.expression.Expression
import printscript.ast.expression.NumberLiteralExpression
import printscript.ast.statement.AssignmentStatement
import printscript.ast.statement.PrintlnStatement
import printscript.ast.statement.Statement
import printscript.ast.statement.VariableDeclarationStatement
import printscript.interpreter.environment.Environment
import printscript.interpreter.environment.MapEnvironment
import printscript.interpreter.statements.AssignmentExecutor
import printscript.interpreter.statements.DeclarationExecutor
import printscript.interpreter.statements.PrintlnExecutor
import printscript.interpreter.statements.StatementExecutor
import printscript.interpreter.statements.StatementExecutorDispatcher
import printscript.interpreter.value.RuntimeValue
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

    private val resultingEnvironment: Environment = MapEnvironment()

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
                result = ExecutionResult.Success(resultingEnvironment),
            )

        val selectedExecutor =
            RecordingExecutor(
                supportsStatement = true,
                result = ExecutionResult.Success(resultingEnvironment),
            )

        val executorAfterMatch =
            RecordingExecutor(
                supportsStatement = true,
                result = ExecutionResult.Success(resultingEnvironment),
            )

        val dispatcher = StatementExecutorDispatcher(
            executors = listOf(
                nonMatchingExecutor,
                selectedExecutor,
                executorAfterMatch,
            ),
        )

        val result = dispatcher.dispatchToExecutor(
            statement = printlnStatement,
            context = executionContext(),
        )

        assertEquals(
            expected = ExecutionResult.Success(resultingEnvironment),
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
        val expectedFailure: ExecutionResult<Environment> =
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

        val result = dispatcher.dispatchToExecutor(
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

        val result = dispatcher.dispatchToExecutor(
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
            declarationExecutor.supportsStatement(declarationStatement),
        )
        assertFalse(
            declarationExecutor.supportsStatement(assignmentStatement),
        )
        assertFalse(
            declarationExecutor.supportsStatement(printlnStatement),
        )

        assertTrue(
            assignmentExecutor.supportsStatement(assignmentStatement),
        )
        assertFalse(
            assignmentExecutor.supportsStatement(declarationStatement),
        )
        assertFalse(
            assignmentExecutor.supportsStatement(printlnStatement),
        )

        assertTrue(
            printlnExecutor.supportsStatement(printlnStatement),
        )
        assertFalse(
            printlnExecutor.supportsStatement(declarationStatement),
        )
        assertFalse(
            printlnExecutor.supportsStatement(assignmentStatement),
        )
    }

    private fun executionContext(): ExecutionContext {
        return UnusedExecutionContext
    }

    /**
     * Ejecutor falso que solo registra si lo llamaron y devuelve lo que
     * le indiquen. Sirve para probar a quién elige el dispatcher, sin
     * depender de la lógica de ningún ejecutor real.
     */
    private class RecordingExecutor(
        private val supportsStatement: Boolean,
        private val result: ExecutionResult<Environment>,
    ) : StatementExecutor {

        var executionCount: Int = 0
            private set

        override fun supportsStatement(statement: Statement): Boolean {
            return supportsStatement
        }

        override fun executeStatement(statement: Statement, context: ExecutionContext): ExecutionResult<Environment> {
            executionCount++

            return result
        }
    }

    private object UnusedExecutionContext : ExecutionContext {

        override val environment: Environment
            get() = fail(
                "Dispatcher test must not access the environment",
            )

        override fun evaluateExpression(expression: Expression): ExecutionResult<RuntimeValue> {
            fail(
                "Dispatcher test must not evaluate expressions",
            )
        }

        override fun writeLine(line: String) {
            fail(
                "Dispatcher test must not write output",
            )
        }
    }
}
