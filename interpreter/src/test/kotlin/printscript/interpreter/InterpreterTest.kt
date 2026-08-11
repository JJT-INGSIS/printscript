package printscript.interpreter

import printscript.interpreter.InMemoryOutput
import printscript.model.ast.DeclaredType
import printscript.model.ast.Identifier
import printscript.model.ast.expression.BinaryExpression
import printscript.model.ast.expression.BinaryOperator
import printscript.model.ast.expression.Expression
import printscript.model.ast.expression.IdentifierExpression
import printscript.model.ast.expression.NumberLiteralExpression
import printscript.model.ast.expression.StringLiteralExpression
import printscript.model.ast.expression.StringQuoteStyle
import printscript.model.ast.expression.UnaryExpression
import printscript.model.ast.expression.UnaryOperator
import printscript.model.ast.statement.AssignmentStatement
import printscript.model.ast.statement.PrintlnStatement
import printscript.model.ast.statement.Statement
import printscript.model.ast.statement.VariableDeclarationStatement
import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan
import printscript.statement.ParseError
import printscript.token.Token
import printscript.token.TokenType
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class InterpreterTest {

    private val anySpan = SourceSpan(
        start = SourcePosition(1, 1, 0),
        end = SourcePosition(1, 1, 0),
    )

    private fun name(value: String): Identifier {
        return Identifier(value, anySpan)
    }

    private fun number(value: String): Expression {
        return NumberLiteralExpression(BigDecimal(value), anySpan)
    }

    private fun text(value: String): Expression {
        return StringLiteralExpression(value, StringQuoteStyle.DOUBLE, anySpan)
    }

    private fun variable(value: String): Expression {
        return IdentifierExpression(name(value))
    }

    private fun binary(
        left: Expression,
        operator: BinaryOperator,
        right: Expression,
    ): Expression {
        return BinaryExpression(left, operator, anySpan, right)
    }

    private fun declare(
        variableName: String,
        type: DeclaredType,
        initializer: Expression?,
    ): Statement {
        return VariableDeclarationStatement(name(variableName), type, initializer, anySpan)
    }

    private fun run(vararg statements: Statement): List<String> {
        val output = InMemoryOutput()
        val result = Interpreter(output).interpret(ListStatementSource(statements.toList()))

        assertEquals(InterpretationResult.Success, result)
        return output.lines()
    }

    private fun runExpectingFailure(vararg statements: Statement): SemanticError {
        val output = InMemoryOutput()
        val result = Interpreter(output).interpret(ListStatementSource(statements.toList()))

        assertIs<InterpretationResult.SemanticFailure>(result)
        return result.error
    }

    @Test
    fun `example 1 from the assignment`() {
        val output = run(
            declare("name", DeclaredType.STRING, text("Joe")),
            declare("lastName", DeclaredType.STRING, text("Doe")),
            PrintlnStatement(
                binary(
                    binary(variable("name"), BinaryOperator.ADD, text(" ")),
                    BinaryOperator.ADD,
                    variable("lastName"),
                ),
                anySpan,
            ),
        )

        assertEquals(listOf("Joe Doe"), output)
    }

    @Test
    fun `example 2 from the assignment`() {
        val output = run(
            declare("a", DeclaredType.NUMBER, number("12")),
            declare("b", DeclaredType.NUMBER, number("4")),
            declare(
                "c",
                DeclaredType.NUMBER,
                binary(variable("a"), BinaryOperator.DIVIDE, variable("b")),
            ),
            PrintlnStatement(
                binary(text("Result: "), BinaryOperator.ADD, variable("c")),
                anySpan,
            ),
        )

        assertEquals(listOf("Result: 3"), output)
    }

    @Test
    fun `example 3 from the assignment, with reassignment`() {
        val output = run(
            declare("a", DeclaredType.NUMBER, number("12")),
            declare("b", DeclaredType.NUMBER, number("4")),
            AssignmentStatement(
                name("a"),
                binary(variable("a"), BinaryOperator.DIVIDE, variable("b")),
                anySpan,
            ),
            PrintlnStatement(
                binary(text("Result: "), BinaryOperator.ADD, variable("a")),
                anySpan,
            ),
        )

        assertEquals(listOf("Result: 3"), output)
    }

    @Test
    fun `decimals are preserved`() {
        val output = run(
            PrintlnStatement(
                binary(number("7"), BinaryOperator.DIVIDE, number("2")),
                anySpan,
            ),
        )

        assertEquals(listOf("3.5"), output)
    }

    @Test
    fun `unary minus negates a number`() {
        val output = run(
            PrintlnStatement(
                UnaryExpression(UnaryOperator.MINUS, anySpan, number("5")),
                anySpan,
            ),
        )

        assertEquals(listOf("-5"), output)
    }

    @Test
    fun `using an undeclared variable fails`() {
        val error = runExpectingFailure(PrintlnStatement(variable("x"), anySpan))

        assertEquals(SemanticError.UndeclaredVariable("x", anySpan), error)
    }

    @Test
    fun `declaring the same variable twice fails`() {
        val error = runExpectingFailure(
            declare("x", DeclaredType.NUMBER, number("1")),
            declare("x", DeclaredType.NUMBER, number("2")),
        )

        assertEquals(SemanticError.AlreadyDeclaredVariable("x", anySpan), error)
    }

    @Test
    fun `assigning a string to a number fails`() {
        val error = runExpectingFailure(declare("x", DeclaredType.NUMBER, text("hola")))

        assertEquals(
            SemanticError.TypeMismatch(
                name = "x",
                expected = DeclaredType.NUMBER,
                actual = DeclaredType.STRING,
                span = anySpan,
            ),
            error,
        )
    }

    @Test
    fun `using a declared but uninitialized variable fails`() {
        val error = runExpectingFailure(
            declare("x", DeclaredType.NUMBER, null),
            PrintlnStatement(variable("x"), anySpan),
        )

        assertEquals(SemanticError.UninitializedVariable("x", anySpan), error)
    }

    @Test
    fun `subtracting strings fails`() {
        val error = runExpectingFailure(
            PrintlnStatement(
                binary(text("a"), BinaryOperator.SUBTRACT, text("b")),
                anySpan,
            ),
        )

        assertEquals(
            SemanticError.InvalidBinaryOperands(
                operator = BinaryOperator.SUBTRACT,
                left = DeclaredType.STRING,
                right = DeclaredType.STRING,
                span = anySpan,
            ),
            error,
        )
    }

    @Test
    fun `unary minus on a string fails`() {
        val error = runExpectingFailure(
            PrintlnStatement(
                UnaryExpression(UnaryOperator.MINUS, anySpan, text("hola")),
                anySpan,
            ),
        )

        assertEquals(
            SemanticError.InvalidUnaryOperand(
                operator = UnaryOperator.MINUS,
                operand = DeclaredType.STRING,
                span = anySpan,
            ),
            error,
        )
    }

    @Test
    fun `dividing by zero fails`() {
        val error = runExpectingFailure(
            PrintlnStatement(
                binary(number("1"), BinaryOperator.DIVIDE, number("0")),
                anySpan,
            ),
        )

        assertEquals(SemanticError.DivisionByZero(anySpan), error)
    }

    @Test
    fun `a parse error is propagated without executing anything`() {
        val parseError = ParseError.UnexpectedToken(
            expected = setOf(TokenType.SEMICOLON),
            actual = Token(TokenType.LET, "let", anySpan),
        )

        val output = InMemoryOutput()
        val result = Interpreter(output).interpret(FailingStatementSource(parseError))

        assertIs<InterpretationResult.ParseFailure>(result)
        assertEquals(parseError, result.error)
        assertEquals(emptyList(), output.lines())
    }

    @Test
    fun `an empty program finishes successfully`() {
        val result = Interpreter(InMemoryOutput()).interpret(ListStatementSource(emptyList()))

        assertEquals(InterpretationResult.Success, result)
    }
}