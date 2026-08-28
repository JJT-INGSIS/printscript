package printscript.cli

import printscript.ast.DeclaredType
import printscript.ast.expression.BinaryOperator
import printscript.ast.expression.UnaryOperator
import printscript.cli.internal.report.ErrorReporter
import printscript.formatter.FormattingError
import printscript.interpreter.SemanticError
import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan
import printscript.source.SourceAccessError
import printscript.statement.ParseError
import printscript.token.LexicalError
import printscript.token.Token
import printscript.token.TokenType
import printscript.v1.lexer.PrintScriptV1LexicalError
import printscript.v1.token.PrintScriptV1TokenType
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertTrue

class ErrorReporterTest {

    private val reporter = ErrorReporter()

    private val anySpan = SourceSpan(
        start = SourcePosition(line = 3, column = 5, offset = 20),
        end = SourcePosition(line = 3, column = 9, offset = 24),
    )

    private val anyPath: Path = Path.of("ejemplo.ps")

    private fun tokenOf(type: TokenType, lexeme: String) = Token(type = type, lexeme = lexeme, span = anySpan)

    // --- acceso al archivo -------------------------------------------

    @Test
    fun `describes every source access error mentioning the path`() {
        val errors = listOf(
            SourceAccessError.NotFound(anyPath),
            SourceAccessError.NotAFile(anyPath),
            SourceAccessError.NotReadable(anyPath),
            SourceAccessError.ReadFailed(anyPath, "disco desconectado"),
        )

        for (error in errors) {
            val message = reporter.describe(error)

            assertContains(message, "error:")
            assertContains(message, "ejemplo.ps")
        }
    }

    @Test
    fun `source access errors have no position because nothing was read`() {
        val message = reporter.describe(SourceAccessError.NotFound(anyPath))

        assertTrue(!message.contains("línea"))
    }

    // --- errores de parseo -------------------------------------------

    @Test
    fun `describes every lexical error`() {
        val errors = listOf(
            LexicalError.UnexpectedCharacter(character = '@', span = anySpan),
            PrintScriptV1LexicalError.UnterminatedString(openingQuote = '"', span = anySpan),
            PrintScriptV1LexicalError.InvalidNumber(lexeme = "1..2", span = anySpan),
        )

        for (error in errors) {
            val message = reporter.describe(ParseError.Lexical(error))

            assertContains(message, "error:")
            assertContains(message, "línea 3")
        }
    }

    @Test
    fun `describes an unexpected token naming every expected one`() {
        val message = reporter.describe(
            ParseError.UnexpectedToken(
                expected = PrintScriptV1TokenType.entries.toSet(),
                actual = tokenOf(PrintScriptV1TokenType.LET, "let"),
            ),
        )

        assertContains(message, "se esperaba")
        assertContains(message, "pero se encontró 'let'")
        assertContains(message, "línea 3")
    }

    @Test
    fun `describes an invalid literal`() {
        val message = reporter.describe(
            ParseError.InvalidLiteral(tokenOf(PrintScriptV1TokenType.NUMBER_LITERAL, "1..2")),
        )

        assertContains(message, "'1..2' no es válido")
    }

    @Test
    fun `describes parse errors implemented by extensions`() {
        val message = reporter.describe(ExtensionParseError(anySpan))

        assertContains(message, "error sintáctico desconocido")
        assertContains(message, "línea 3")
    }

    @Test
    fun `describes extension token types using their representation`() {
        val message = reporter.describe(
            ParseError.UnexpectedToken(
                expected = setOf(ExtensionTokenType),
                actual = tokenOf(PrintScriptV1TokenType.EOF, ""),
            ),
        )

        assertContains(message, ExtensionTokenType.toString())
    }

    // --- errores semánticos ------------------------------------------

    @Test
    fun `describes every semantic error with its position`() {
        val errors = listOf(
            SemanticError.UndeclaredVariable(name = "x", span = anySpan),
            SemanticError.UninitializedVariable(name = "x", span = anySpan),
            SemanticError.AlreadyDeclaredVariable(name = "x", span = anySpan),
            SemanticError.TypeMismatch(
                name = "x",
                expected = DeclaredType.NUMBER,
                actual = DeclaredType.STRING,
                span = anySpan,
            ),
            SemanticError.InvalidBinaryOperands(
                operator = BinaryOperator.SUBTRACT,
                left = DeclaredType.STRING,
                right = DeclaredType.STRING,
                span = anySpan,
            ),
            SemanticError.InvalidUnaryOperand(
                operator = UnaryOperator.MINUS,
                operand = DeclaredType.STRING,
                span = anySpan,
            ),
            SemanticError.DivisionByZero(span = anySpan),
            SemanticError.UnsupportedBinaryOperator(
                operator = BinaryOperator.MULTIPLY,
                span = anySpan,
            ),
            SemanticError.UnsupportedStatement(span = anySpan),
        )

        for (error in errors) {
            val message = reporter.describe(error)

            assertContains(message, "error:")
            assertContains(message, "línea 3, columnas 5 a 9")
        }
    }

    @Test
    fun `describes both operand types on a binary mismatch`() {
        val message = reporter.describe(
            SemanticError.InvalidBinaryOperands(
                operator = BinaryOperator.DIVIDE,
                left = DeclaredType.STRING,
                right = DeclaredType.NUMBER,
                span = anySpan,
            ),
        )

        assertContains(message, "'/'")
        assertContains(message, "entre string y number")
    }

    @Test
    fun `describes every unary operator`() {
        val operators = listOf(UnaryOperator.PLUS, UnaryOperator.MINUS)

        for (operator in operators) {
            val message = reporter.describe(
                SemanticError.InvalidUnaryOperand(
                    operator = operator,
                    operand = DeclaredType.STRING,
                    span = anySpan,
                ),
            )

            assertContains(message, "no se puede aplicar")
        }
    }

    @Test
    fun `describes every binary operator`() {
        for (operator in BinaryOperator.entries) {
            val message = reporter.describe(
                SemanticError.UnsupportedBinaryOperator(
                    operator = operator,
                    span = anySpan,
                ),
            )

            assertContains(message, "no está soportado")
        }
    }

    @Test
    fun `describes formatting errors implemented by extensions`() {
        val message = reporter.describe(ExtensionFormattingError(anySpan))

        assertContains(message, "error de formateo desconocido")
        assertContains(message, "línea 3")
    }

    private object ExtensionTokenType : TokenType {

        override fun toString(): String {
            return "EXTENSION_TOKEN"
        }
    }

    private data class ExtensionParseError(
        override val span: SourceSpan,
    ) : ParseError

    private data class ExtensionFormattingError(
        override val span: SourceSpan,
    ) : FormattingError
}
