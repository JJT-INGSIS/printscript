package printscript.interpreter

import printscript.ast.DeclaredType
import printscript.ast.Identifier
import printscript.ast.expression.BinaryExpression
import printscript.ast.expression.BinaryOperator
import printscript.ast.expression.Expression
import printscript.ast.expression.IdentifierExpression
import printscript.ast.expression.NumberLiteralExpression
import printscript.ast.expression.StringLiteralExpression
import printscript.ast.expression.StringQuoteStyle
import printscript.ast.expression.UnaryExpression
import printscript.ast.expression.UnaryOperator
import printscript.ast.statement.AssignmentStatement
import printscript.ast.statement.PrintlnStatement
import printscript.ast.statement.Statement
import printscript.ast.statement.VariableDeclarationStatement
import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan
import printscript.statement.ParseError
import printscript.token.PrintScriptV1TokenType
import printscript.token.Token
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class PrintScriptInterpreterTest {

    private val anySpan = SourceSpan(
        start = SourcePosition(1, 1, 0),
        end = SourcePosition(1, 1, 0),
    )

    private fun name(value: String): Identifier {
        return Identifier(
            value = value,
            span = anySpan,
        )
    }

    private fun number(value: String): Expression {
        return NumberLiteralExpression(
            value = BigDecimal(value),
            span = anySpan,
        )
    }

    private fun text(value: String): Expression {
        return StringLiteralExpression(
            value = value,
            quoteStyle = StringQuoteStyle.DOUBLE,
            span = anySpan,
        )
    }

    private fun variable(value: String): Expression {
        return IdentifierExpression(
            identifier = name(value),
        )
    }

    private fun binary(left: Expression, operator: BinaryOperator, right: Expression): Expression {
        return BinaryExpression(
            left = left,
            operator = operator,
            operatorSpan = anySpan,
            right = right,
        )
    }

    private fun declare(variableName: String, type: DeclaredType, initializer: Expression?): Statement {
        return VariableDeclarationStatement(
            identifier = name(variableName),
            declaredType = type,
            initializer = initializer,
            span = anySpan,
        )
    }

    private fun createInterpreter(output: InMemoryOutput): Interpreter {
        return PrintScriptInterpreterFactory.createV1(
            output = output,
        )
    }

    private fun run(vararg statements: Statement): List<String> {
        val output = InMemoryOutput()

        val result = createInterpreter(output).interpret(
            source = ListStatementSource(
                statements.toList(),
            ),
        )

        assertEquals(
            expected = InterpretationResult.Success,
            actual = result,
        )

        return output.lines()
    }

    private fun runExpectingFailure(vararg statements: Statement): SemanticError {
        val output = InMemoryOutput()

        val result = createInterpreter(output).interpret(
            source = ListStatementSource(
                statements.toList(),
            ),
        )

        val failure =
            assertIs<InterpretationResult.SemanticFailure>(result)

        return failure.error
    }

    @Test
    fun `example 1 from the assignment`() {
        val output = run(
            declare(
                variableName = "name",
                type = DeclaredType.STRING,
                initializer = text("Joe"),
            ),
            declare(
                variableName = "lastName",
                type = DeclaredType.STRING,
                initializer = text("Doe"),
            ),
            PrintlnStatement(
                argument = binary(
                    left = binary(
                        left = variable("name"),
                        operator = BinaryOperator.ADD,
                        right = text(" "),
                    ),
                    operator = BinaryOperator.ADD,
                    right = variable("lastName"),
                ),
                span = anySpan,
            ),
        )

        assertEquals(
            expected = listOf("Joe Doe"),
            actual = output,
        )
    }

    @Test
    fun `example 2 from the assignment`() {
        val output = run(
            declare(
                variableName = "a",
                type = DeclaredType.NUMBER,
                initializer = number("12"),
            ),
            declare(
                variableName = "b",
                type = DeclaredType.NUMBER,
                initializer = number("4"),
            ),
            declare(
                variableName = "c",
                type = DeclaredType.NUMBER,
                initializer = binary(
                    left = variable("a"),
                    operator = BinaryOperator.DIVIDE,
                    right = variable("b"),
                ),
            ),
            PrintlnStatement(
                argument = binary(
                    left = text("Result: "),
                    operator = BinaryOperator.ADD,
                    right = variable("c"),
                ),
                span = anySpan,
            ),
        )

        assertEquals(
            expected = listOf("Result: 3"),
            actual = output,
        )
    }

    @Test
    fun `example 3 from the assignment, with reassignment`() {
        val output = run(
            declare(
                variableName = "a",
                type = DeclaredType.NUMBER,
                initializer = number("12"),
            ),
            declare(
                variableName = "b",
                type = DeclaredType.NUMBER,
                initializer = number("4"),
            ),
            AssignmentStatement(
                target = name("a"),
                expression = binary(
                    left = variable("a"),
                    operator = BinaryOperator.DIVIDE,
                    right = variable("b"),
                ),
                span = anySpan,
            ),
            PrintlnStatement(
                argument = binary(
                    left = text("Result: "),
                    operator = BinaryOperator.ADD,
                    right = variable("a"),
                ),
                span = anySpan,
            ),
        )

        assertEquals(
            expected = listOf("Result: 3"),
            actual = output,
        )
    }

    @Test
    fun `decimals are preserved`() {
        val output = run(
            PrintlnStatement(
                argument = binary(
                    left = number("7"),
                    operator = BinaryOperator.DIVIDE,
                    right = number("2"),
                ),
                span = anySpan,
            ),
        )

        assertEquals(
            expected = listOf("3.5"),
            actual = output,
        )
    }

    @Test
    fun `unary minus negates a number`() {
        val output = run(
            PrintlnStatement(
                argument = UnaryExpression(
                    operator = UnaryOperator.MINUS,
                    operatorSpan = anySpan,
                    operand = number("5"),
                ),
                span = anySpan,
            ),
        )

        assertEquals(
            expected = listOf("-5"),
            actual = output,
        )
    }

    @Test
    fun `using an undeclared variable fails`() {
        val error = runExpectingFailure(
            PrintlnStatement(
                argument = variable("x"),
                span = anySpan,
            ),
        )

        assertEquals(
            expected = SemanticError.UndeclaredVariable(
                name = "x",
                span = anySpan,
            ),
            actual = error,
        )
    }

    @Test
    fun `declaring the same variable twice fails`() {
        val error = runExpectingFailure(
            declare(
                variableName = "x",
                type = DeclaredType.NUMBER,
                initializer = number("1"),
            ),
            declare(
                variableName = "x",
                type = DeclaredType.NUMBER,
                initializer = number("2"),
            ),
        )

        assertEquals(
            expected = SemanticError.AlreadyDeclaredVariable(
                name = "x",
                span = anySpan,
            ),
            actual = error,
        )
    }

    @Test
    fun `assigning a string to a number fails`() {
        val error = runExpectingFailure(
            declare(
                variableName = "x",
                type = DeclaredType.NUMBER,
                initializer = text("hola"),
            ),
        )

        assertEquals(
            expected = SemanticError.TypeMismatch(
                name = "x",
                expected = DeclaredType.NUMBER,
                actual = DeclaredType.STRING,
                span = anySpan,
            ),
            actual = error,
        )
    }

    @Test
    fun `using a declared but uninitialized variable fails`() {
        val error = runExpectingFailure(
            declare(
                variableName = "x",
                type = DeclaredType.NUMBER,
                initializer = null,
            ),
            PrintlnStatement(
                argument = variable("x"),
                span = anySpan,
            ),
        )

        assertEquals(
            expected = SemanticError.UninitializedVariable(
                name = "x",
                span = anySpan,
            ),
            actual = error,
        )
    }

    @Test
    fun `subtracting strings fails`() {
        val error = runExpectingFailure(
            PrintlnStatement(
                argument = binary(
                    left = text("a"),
                    operator = BinaryOperator.SUBTRACT,
                    right = text("b"),
                ),
                span = anySpan,
            ),
        )

        assertEquals(
            expected = SemanticError.InvalidBinaryOperands(
                operator = BinaryOperator.SUBTRACT,
                left = DeclaredType.STRING,
                right = DeclaredType.STRING,
                span = anySpan,
            ),
            actual = error,
        )
    }

    @Test
    fun `unary minus on a string fails`() {
        val error = runExpectingFailure(
            PrintlnStatement(
                argument = UnaryExpression(
                    operator = UnaryOperator.MINUS,
                    operatorSpan = anySpan,
                    operand = text("hola"),
                ),
                span = anySpan,
            ),
        )

        assertEquals(
            expected = SemanticError.InvalidUnaryOperand(
                operator = UnaryOperator.MINUS,
                operand = DeclaredType.STRING,
                span = anySpan,
            ),
            actual = error,
        )
    }

    @Test
    fun `dividing by zero fails`() {
        val error = runExpectingFailure(
            PrintlnStatement(
                argument = binary(
                    left = number("1"),
                    operator = BinaryOperator.DIVIDE,
                    right = number("0"),
                ),
                span = anySpan,
            ),
        )

        assertEquals(
            expected = SemanticError.DivisionByZero(
                span = anySpan,
            ),
            actual = error,
        )
    }

    @Test
    fun `a parse error is propagated without executing anything`() {
        val parseError = ParseError.UnexpectedToken(
            expected = setOf(PrintScriptV1TokenType.SEMICOLON),
            actual = Token(
                type = PrintScriptV1TokenType.LET,
                lexeme = "let",
                span = anySpan,
            ),
        )

        val output = InMemoryOutput()

        val result = createInterpreter(output).interpret(
            source = FailingStatementSource(parseError),
        )

        val failure =
            assertIs<InterpretationResult.ParseFailure>(result)

        assertEquals(
            expected = parseError,
            actual = failure.error,
        )

        assertEquals(
            expected = emptyList(),
            actual = output.lines(),
        )
    }

    @Test
    fun `an empty program finishes successfully`() {
        val result = createInterpreter(
            output = InMemoryOutput(),
        ).interpret(
            source = ListStatementSource(emptyList()),
        )

        assertEquals(
            expected = InterpretationResult.Success,
            actual = result,
        )
    }
}
