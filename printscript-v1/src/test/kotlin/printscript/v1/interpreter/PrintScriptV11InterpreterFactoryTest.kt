package printscript.v1.interpreter

import printscript.ast.DeclarationKind
import printscript.ast.DeclaredType
import printscript.ast.Identifier
import printscript.ast.expression.BinaryExpression
import printscript.ast.expression.BinaryOperator
import printscript.ast.expression.BooleanLiteralExpression
import printscript.ast.expression.Expression
import printscript.ast.expression.IdentifierExpression
import printscript.ast.expression.NumberLiteralExpression
import printscript.ast.expression.ReadEnvironmentExpression
import printscript.ast.expression.ReadInputExpression
import printscript.ast.expression.StringLiteralExpression
import printscript.ast.expression.StringQuoteStyle
import printscript.ast.statement.AssignmentStatement
import printscript.ast.statement.BlockStatement
import printscript.ast.statement.IfStatement
import printscript.ast.statement.PrintlnStatement
import printscript.ast.statement.VariableDeclarationStatement
import printscript.interpreter.InterpretationResult
import printscript.interpreter.Interpreter
import printscript.interpreter.SemanticError
import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan
import printscript.runtime.EnvironmentVariableProvider
import printscript.runtime.ProgramInput
import printscript.statement.Statement
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class PrintScriptV11InterpreterFactoryTest {

    private val anySpan = SourceSpan(
        start = SourcePosition(1, 1, 0),
        end = SourcePosition(1, 1, 0),
    )

    @Test
    fun `evaluates boolean literals`() {
        val execution = runSuccessfully(
            declare("active", DeclaredType.BOOLEAN, boolean(true)),
            print(variable("active")),
        )

        assertEquals(expected = listOf("true"), actual = execution.output.lines())
    }

    @Test
    fun `reads a string from program input`() {
        val input = QueueProgramInput(listOf("Ada"))

        val execution = runSuccessfully(
            declare("name", DeclaredType.STRING, readInput(text("Name:"))),
            print(variable("name")),
            input = input,
        )

        assertEquals(expected = listOf("Name:"), actual = input.prompts())
        assertEquals(expected = listOf("Ada"), actual = execution.output.lines())
    }

    @Test
    fun `interprets program input using the declared type`() {
        val execution = runSuccessfully(
            declare("age", DeclaredType.NUMBER, readInput(text("Age:"))),
            declare("active", DeclaredType.BOOLEAN, readInput(text("Active:"))),
            print(variable("age")),
            print(variable("active")),
            input = QueueProgramInput(listOf("21", "false")),
        )

        assertEquals(expected = listOf("21", "false"), actual = execution.output.lines())
    }

    @Test
    fun `interprets program input using the assigned variable type`() {
        val execution = runSuccessfully(
            declare("age", DeclaredType.NUMBER, null),
            assign("age", readInput(text("Age:"))),
            print(variable("age")),
            input = QueueProgramInput(listOf("21")),
        )

        assertEquals(expected = listOf("21"), actual = execution.output.lines())
    }

    @Test
    fun `propagates the expected type into nested input expressions`() {
        val execution = runSuccessfully(
            declare(
                variableName = "result",
                type = DeclaredType.NUMBER,
                initializer = binary(
                    left = readInput(text("Number:")),
                    operator = BinaryOperator.ADD,
                    right = number("5"),
                ),
            ),
            print(variable("result")),
            input = QueueProgramInput(listOf("7")),
        )

        assertEquals(expected = listOf("12"), actual = execution.output.lines())
    }

    @Test
    fun `uses string as the default input type without a typed context`() {
        val execution = runSuccessfully(
            print(readInput(text("Value:"))),
            input = QueueProgramInput(listOf("hello")),
        )

        assertEquals(expected = listOf("hello"), actual = execution.output.lines())
    }

    @Test
    fun `reports an input value that cannot be interpreted`() {
        val error = runExpectingFailure(
            declare("active", DeclaredType.BOOLEAN, readInput(text("Active:"))),
            input = QueueProgramInput(listOf("not-a-boolean")),
        )

        assertEquals(
            expected = PrintScriptV1SemanticError.InvalidInputValue(
                expected = DeclaredType.BOOLEAN,
                span = anySpan,
            ),
            actual = error,
        )
    }

    @Test
    fun `reports unavailable program input`() {
        val error = runExpectingFailure(
            declare("name", DeclaredType.STRING, readInput(text("Name:"))),
        )

        assertEquals(
            expected = PrintScriptV1SemanticError.InputUnavailable(span = anySpan),
            actual = error,
        )
    }

    @Test
    fun `requires a string input prompt`() {
        val error = runExpectingFailure(
            declare("name", DeclaredType.STRING, readInput(number("1"))),
            input = QueueProgramInput(listOf("unused")),
        )

        assertEquals(
            expected = PrintScriptV1SemanticError.InvalidInputPrompt(
                actual = DeclaredType.NUMBER,
                span = anySpan,
            ),
            actual = error,
        )
    }

    @Test
    fun `reads environment variables as strings`() {
        val execution = runSuccessfully(
            declare("club", DeclaredType.STRING, readEnvironment(text("CLUB"))),
            print(variable("club")),
            environmentVariables = MapEnvironmentVariables(
                mapOf("CLUB" to "San Lorenzo"),
            ),
        )

        assertEquals(expected = listOf("San Lorenzo"), actual = execution.output.lines())
    }

    @Test
    fun `converts environment variables to the declared type`() {
        val execution = runSuccessfully(
            declare("port", DeclaredType.NUMBER, readEnvironment(text("PORT"))),
            declare("active", DeclaredType.BOOLEAN, readEnvironment(text("ACTIVE"))),
            print(variable("port")),
            print(variable("active")),
            environmentVariables = MapEnvironmentVariables(
                mapOf(
                    "PORT" to "8080",
                    "ACTIVE" to "true",
                ),
            ),
        )

        assertEquals(expected = listOf("8080", "true"), actual = execution.output.lines())
    }

    @Test
    fun `reports an environment variable that cannot be converted`() {
        val error = runExpectingFailure(
            declare("port", DeclaredType.NUMBER, readEnvironment(text("PORT"))),
            environmentVariables = MapEnvironmentVariables(mapOf("PORT" to "not a number")),
        )

        assertEquals(
            expected = PrintScriptV1SemanticError.InvalidEnvironmentVariableValue(
                name = "PORT",
                expected = DeclaredType.NUMBER,
                span = anySpan,
            ),
            actual = error,
        )
    }

    @Test
    fun `reports a missing environment variable`() {
        val error = runExpectingFailure(
            declare("value", DeclaredType.STRING, readEnvironment(text("MISSING"))),
        )

        assertEquals(
            expected = PrintScriptV1SemanticError.EnvironmentVariableNotFound(
                name = "MISSING",
                span = anySpan,
            ),
            actual = error,
        )
    }

    @Test
    fun `requires a string environment variable name`() {
        val error = runExpectingFailure(
            declare("value", DeclaredType.STRING, readEnvironment(number("1"))),
        )

        assertEquals(
            expected = PrintScriptV1SemanticError.InvalidEnvironmentVariableName(
                actual = DeclaredType.NUMBER,
                span = anySpan,
            ),
            actual = error,
        )
    }

    @Test
    fun `executes the matching if branch`() {
        val trueExecution = runSuccessfully(
            declare("condition", DeclaredType.BOOLEAN, boolean(true)),
            conditional(
                conditionName = "condition",
                thenStatements = listOf(print(text("then"))),
                elseStatements = listOf(print(text("else"))),
            ),
        )
        val falseExecution = runSuccessfully(
            declare("condition", DeclaredType.BOOLEAN, boolean(false)),
            conditional(
                conditionName = "condition",
                thenStatements = listOf(print(text("then"))),
                elseStatements = listOf(print(text("else"))),
            ),
        )

        assertEquals(expected = listOf("then"), actual = trueExecution.output.lines())
        assertEquals(expected = listOf("else"), actual = falseExecution.output.lines())
    }

    @Test
    fun `continues when a false if has no else branch`() {
        val execution = runSuccessfully(
            declare("condition", DeclaredType.BOOLEAN, boolean(false)),
            conditional(
                conditionName = "condition",
                thenStatements = listOf(print(text("not printed"))),
            ),
            print(text("continued")),
        )

        assertEquals(expected = listOf("continued"), actual = execution.output.lines())
    }

    @Test
    fun `preserves assignments to outer variables after leaving a branch`() {
        val execution = runSuccessfully(
            declare("condition", DeclaredType.BOOLEAN, boolean(true)),
            declare("result", DeclaredType.NUMBER, number("1")),
            conditional(
                conditionName = "condition",
                thenStatements = listOf(assign("result", number("2"))),
            ),
            print(variable("result")),
        )

        assertEquals(expected = listOf("2"), actual = execution.output.lines())
    }

    @Test
    fun `removes variables declared inside a branch`() {
        val error = runExpectingFailure(
            declare("condition", DeclaredType.BOOLEAN, boolean(true)),
            conditional(
                conditionName = "condition",
                thenStatements = listOf(declare("local", DeclaredType.NUMBER, number("1"))),
            ),
            print(variable("local")),
        )

        assertEquals(
            expected = PrintScriptV1SemanticError.UndeclaredVariable(
                name = "local",
                span = anySpan,
            ),
            actual = error,
        )
    }

    @Test
    fun `executes nested conditionals`() {
        val execution = runSuccessfully(
            declare("outer", DeclaredType.BOOLEAN, boolean(true)),
            declare("inner", DeclaredType.BOOLEAN, boolean(true)),
            conditional(
                conditionName = "outer",
                thenStatements = listOf(
                    conditional(
                        conditionName = "inner",
                        thenStatements = listOf(print(text("nested"))),
                    ),
                ),
            ),
        )

        assertEquals(expected = listOf("nested"), actual = execution.output.lines())
    }

    @Test
    fun `requires a boolean if condition`() {
        val error = runExpectingFailure(
            declare("condition", DeclaredType.STRING, text("true")),
            conditional(
                conditionName = "condition",
                thenStatements = emptyList(),
            ),
        )

        assertEquals(
            expected = PrintScriptV1SemanticError.InvalidIfCondition(
                name = "condition",
                actual = DeclaredType.STRING,
                span = anySpan,
            ),
            actual = error,
        )
    }

    @Test
    fun `reports unavailable if conditions`() {
        val undeclared = runExpectingFailure(
            conditional(
                conditionName = "missing",
                thenStatements = emptyList(),
            ),
        )
        val uninitialized = runExpectingFailure(
            declare("condition", DeclaredType.BOOLEAN, null),
            conditional(
                conditionName = "condition",
                thenStatements = emptyList(),
            ),
        )

        assertIs<PrintScriptV1SemanticError.UndeclaredVariable>(undeclared)
        assertIs<PrintScriptV1SemanticError.UninitializedVariable>(uninitialized)
    }

    @Test
    fun `propagates semantic failures from a branch`() {
        val error = runExpectingFailure(
            declare("condition", DeclaredType.BOOLEAN, boolean(true)),
            conditional(
                conditionName = "condition",
                thenStatements = listOf(print(variable("missing"))),
            ),
        )

        assertEquals(
            expected = PrintScriptV1SemanticError.UndeclaredVariable(
                name = "missing",
                span = anySpan,
            ),
            actual = error,
        )
    }

    @Test
    fun `supports constant declarations`() {
        val execution = runSuccessfully(
            declare(
                variableName = "answer",
                type = DeclaredType.NUMBER,
                initializer = number("42"),
                declarationKind = DeclarationKind.CONSTANT,
            ),
            print(variable("answer")),
        )

        assertEquals(expected = listOf("42"), actual = execution.output.lines())
    }

    @Test
    fun `the V1 interpreter still rejects V1_1 statements`() {
        val interpreter = PrintScriptV1InterpreterFactory.create(InMemoryOutput())
        val result = interpreter.interpret(
            ListStatementSource(
                listOf(
                    conditional(
                        conditionName = "condition",
                        thenStatements = emptyList(),
                    ),
                ),
            ),
        )

        val failure = assertIs<InterpretationResult.SemanticFailure>(result)
        assertIs<SemanticError.UnsupportedStatement>(failure.error)
    }

    private fun runSuccessfully(
        vararg statements: Statement,
        input: QueueProgramInput = QueueProgramInput(emptyList()),
        environmentVariables: EnvironmentVariableProvider = MapEnvironmentVariables(emptyMap()),
    ): SuccessfulExecution {
        val output = InMemoryOutput()
        val result = interpreterWith(output, input, environmentVariables).interpret(
            ListStatementSource(statements.toList()),
        )

        assertEquals(expected = InterpretationResult.Success, actual = result)
        return SuccessfulExecution(output)
    }

    private fun runExpectingFailure(
        vararg statements: Statement,
        input: QueueProgramInput = QueueProgramInput(emptyList()),
        environmentVariables: EnvironmentVariableProvider = MapEnvironmentVariables(emptyMap()),
    ): SemanticError {
        val result = interpreterWith(
            output = InMemoryOutput(),
            input = input,
            environmentVariables = environmentVariables,
        ).interpret(
            ListStatementSource(statements.toList()),
        )

        return assertIs<InterpretationResult.SemanticFailure>(result).error
    }

    private fun interpreterWith(
        output: InMemoryOutput,
        input: ProgramInput,
        environmentVariables: EnvironmentVariableProvider,
    ): Interpreter {
        return PrintScriptV11InterpreterFactory.create(
            output = output,
            input = input,
            environmentVariables = environmentVariables,
        )
    }

    private fun declare(
        variableName: String,
        type: DeclaredType,
        initializer: Expression?,
        declarationKind: DeclarationKind = DeclarationKind.VARIABLE,
    ): VariableDeclarationStatement {
        return VariableDeclarationStatement(
            identifier = name(variableName),
            declaredType = type,
            initializer = initializer,
            span = anySpan,
            declarationKind = declarationKind,
        )
    }

    private fun assign(variableName: String, expression: Expression): AssignmentStatement {
        return AssignmentStatement(
            target = name(variableName),
            expression = expression,
            span = anySpan,
        )
    }

    private fun conditional(
        conditionName: String,
        thenStatements: List<Statement>,
        elseStatements: List<Statement>? = null,
    ): IfStatement {
        return IfStatement(
            condition = name(conditionName),
            thenBranch = BlockStatement(thenStatements, anySpan),
            elseBranch = elseStatements?.let { BlockStatement(it, anySpan) },
            span = anySpan,
        )
    }

    private fun print(expression: Expression): PrintlnStatement {
        return PrintlnStatement(argument = expression, span = anySpan)
    }

    private fun name(value: String): Identifier {
        return Identifier(value = value, span = anySpan)
    }

    private fun variable(value: String): IdentifierExpression {
        return IdentifierExpression(name(value))
    }

    private fun number(value: String): NumberLiteralExpression {
        return NumberLiteralExpression(BigDecimal(value), anySpan)
    }

    private fun text(value: String): StringLiteralExpression {
        return StringLiteralExpression(value, StringQuoteStyle.DOUBLE, anySpan)
    }

    private fun boolean(value: Boolean): BooleanLiteralExpression {
        return BooleanLiteralExpression(value, anySpan)
    }

    private fun readInput(prompt: Expression): ReadInputExpression {
        return ReadInputExpression(prompt, anySpan)
    }

    private fun readEnvironment(variableName: Expression): ReadEnvironmentExpression {
        return ReadEnvironmentExpression(variableName, anySpan)
    }

    private fun binary(left: Expression, operator: BinaryOperator, right: Expression): BinaryExpression {
        return BinaryExpression(left, operator, anySpan, right)
    }

    private data class SuccessfulExecution(
        val output: InMemoryOutput,
    )
}

private class QueueProgramInput(
    values: List<String>,
) : ProgramInput {

    private val values = values.toList()
    private val receivedPrompts = mutableListOf<String>()
    private var nextValueIndex = 0

    override fun readLine(prompt: String): String? {
        receivedPrompts.add(prompt)
        val value = values.getOrNull(nextValueIndex) ?: return null
        nextValueIndex += 1
        return value
    }

    fun prompts(): List<String> {
        return receivedPrompts.toList()
    }
}

private class MapEnvironmentVariables(
    private val values: Map<String, String>,
) : EnvironmentVariableProvider {

    override fun valueOf(name: String): String? {
        return values[name]
    }
}
