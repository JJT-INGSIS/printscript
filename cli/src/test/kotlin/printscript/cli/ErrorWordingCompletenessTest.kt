package printscript.cli

import printscript.ast.DeclaredType
import printscript.ast.Identifier
import printscript.ast.expression.BinaryOperator
import printscript.ast.expression.NumberLiteralExpression
import printscript.ast.expression.UnaryOperator
import printscript.cli.internal.report.DiagnosticReporter
import printscript.cli.internal.report.ErrorReporter
import printscript.cli.internal.report.PrintScriptWording
import printscript.formatter.FormattingError
import printscript.interpreter.SemanticError
import printscript.lexer.SourceReadingError
import printscript.linter.Diagnostic
import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan
import printscript.source.SourceReadError
import printscript.statement.ParseError
import printscript.token.LexicalError
import printscript.token.Token
import printscript.v1.interpreter.PrintScriptV1SemanticError
import printscript.v1.lexer.PrintScriptV1LexicalError
import printscript.v1.linter.PrintScriptV1Diagnostic
import printscript.v1.linter.PrintScriptV1NamingConvention
import printscript.v1.token.PrintScriptV1TokenType
import java.math.BigDecimal
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertNotEquals

class ErrorWordingCompletenessTest {

    private val errorReporter = ErrorReporter()
    private val diagnosticReporter = DiagnosticReporter()

    private val anySpan = SourceSpan(
        start = SourcePosition(line = 3, column = 5, offset = 20),
        end = SourcePosition(line = 3, column = 9, offset = 24),
    )

    @Test
    fun `every V1 token type has its own wording`() {
        for (tokenType in PrintScriptV1TokenType.entries) {
            assertNotEquals(
                illegal = tokenType.toString(),
                actual = PrintScriptWording.describe(tokenType),
                message = "Falta la redacción de $tokenType en PrintScriptWording",
            )
        }
    }

    @Test
    fun `every V1 lexical error has its own wording`() {
        val unknownWording = describeLexical(ExtensionLexicalError(anySpan))

        for (error in everyV1LexicalError) {
            assertNotEquals(
                illegal = unknownWording,
                actual = describeLexical(error),
                message = "Falta la redacción de ${caseNameOf(error)} en ErrorReporter",
            )
        }
    }

    @Test
    fun `the engine's own lexical error has its own wording`() {
        assertNotEquals(
            illegal = describeLexical(ExtensionLexicalError(anySpan)),
            actual = describeLexical(
                LexicalError.UnexpectedCharacter(character = '@', span = anySpan),
            ),
        )
    }

    @Test
    fun `every parse error has its own wording`() {
        val unknownWording = errorReporter.describe(ExtensionParseError(anySpan))

        val errors = listOf(
            ParseError.TokenRead(LexicalError.UnexpectedCharacter(character = '@', span = anySpan)),
            ParseError.TokenRead(
                SourceReadingError(
                    sourceError = SourceReadError.InvalidEncoding(
                        path = Path.of("program.ps"),
                        byteOffset = 0L,
                    ),
                    span = anySpan,
                ),
            ),
            ParseError.TokenRead(
                SourceReadingError(
                    sourceError = SourceReadError.InvalidInputStreamEncoding,
                    span = anySpan,
                ),
            ),
            ParseError.TokenRead(
                SourceReadingError(
                    sourceError = SourceReadError.InputStreamReadFailed("stream disconnected"),
                    span = anySpan,
                ),
            ),
            ParseError.UnexpectedToken(
                expected = setOf(PrintScriptV1TokenType.SEMICOLON),
                actual = tokenOf(PrintScriptV1TokenType.LET, "let"),
            ),
            ParseError.InvalidLiteral(tokenOf(PrintScriptV1TokenType.NUMBER_LITERAL, "1..2")),
        )

        for (error in errors) {
            assertNotEquals(
                illegal = unknownWording,
                actual = errorReporter.describe(error),
                message = "Falta la redacción de ${error::class.simpleName} en ErrorReporter",
            )
        }
    }

    @Test
    fun `every V1 semantic error has its own wording`() {
        val unknownWording = errorReporter.describe(ExtensionSemanticError(anySpan))

        for (error in everyV1SemanticError) {
            assertNotEquals(
                illegal = unknownWording,
                actual = errorReporter.describe(error),
                message = "Falta la redacción de ${caseNameOf(error)} en ErrorReporter",
            )
        }
    }

    @Test
    fun `the engine's own semantic error has its own wording`() {
        assertNotEquals(
            illegal = errorReporter.describe(ExtensionSemanticError(anySpan)),
            actual = errorReporter.describe(SemanticError.UnsupportedStatement(span = anySpan)),
        )
    }

    @Test
    fun `every formatting error has its own wording`() {
        val unknownWording = errorReporter.describe(ExtensionFormattingError(anySpan))

        val errors = listOf(
            FormattingError.TokenReadFailure(
                LexicalError.UnexpectedCharacter(character = '@', span = anySpan),
            ),
        )

        for (error in errors) {
            assertNotEquals(
                illegal = unknownWording,
                actual = errorReporter.describe(error),
                message = "Falta la redacción de ${error::class.simpleName} en ErrorReporter",
            )
        }
    }

    @Test
    fun `every V1 diagnostic has its own wording`() {
        val unknownWording = diagnosticReporter.describe(ExtensionDiagnostic(anySpan))

        for (diagnostic in everyV1Diagnostic) {
            assertNotEquals(
                illegal = unknownWording,
                actual = diagnosticReporter.describe(diagnostic),
                message = "Falta la redacción de ${caseNameOf(diagnostic)} en DiagnosticReporter",
            )
        }
    }

    private val everyV1SemanticError = listOf(
        PrintScriptV1SemanticError.UndeclaredVariable(name = "x", span = anySpan),
        PrintScriptV1SemanticError.UninitializedVariable(name = "x", span = anySpan),
        PrintScriptV1SemanticError.AlreadyDeclaredVariable(name = "x", span = anySpan),
        PrintScriptV1SemanticError.TypeMismatch(
            name = "x",
            expected = DeclaredType.NUMBER,
            actual = DeclaredType.STRING,
            span = anySpan,
        ),
        PrintScriptV1SemanticError.InvalidBinaryOperands(
            operator = BinaryOperator.ADD,
            left = DeclaredType.STRING,
            right = DeclaredType.NUMBER,
            span = anySpan,
        ),
        PrintScriptV1SemanticError.InvalidUnaryOperand(
            operator = UnaryOperator.MINUS,
            operand = DeclaredType.STRING,
            span = anySpan,
        ),
        PrintScriptV1SemanticError.DivisionByZero(span = anySpan),
        PrintScriptV1SemanticError.UnsupportedBinaryOperator(
            operator = BinaryOperator.MULTIPLY,
            span = anySpan,
        ),
    )

    private val everyV1LexicalError = listOf(
        PrintScriptV1LexicalError.UnterminatedString(openingQuote = '"', span = anySpan),
        PrintScriptV1LexicalError.InvalidNumber(lexeme = "1..2", span = anySpan),
    )

    private val everyV1Diagnostic = listOf(
        PrintScriptV1Diagnostic.NamingConventionViolation(
            identifier = Identifier(value = "mi_variable", span = anySpan),
            expectedConvention = PrintScriptV1NamingConvention.CAMEL_CASE,
        ),
        PrintScriptV1Diagnostic.UnsupportedPrintlnArgument(
            argument = NumberLiteralExpression(value = BigDecimal.ONE, span = anySpan),
        ),
    )

    private fun caseNameOf(error: PrintScriptV1SemanticError): String {
        return when (error) {
            is PrintScriptV1SemanticError.UndeclaredVariable -> "UndeclaredVariable"
            is PrintScriptV1SemanticError.UninitializedVariable -> "UninitializedVariable"
            is PrintScriptV1SemanticError.AlreadyDeclaredVariable -> "AlreadyDeclaredVariable"
            is PrintScriptV1SemanticError.TypeMismatch -> "TypeMismatch"
            is PrintScriptV1SemanticError.InvalidBinaryOperands -> "InvalidBinaryOperands"
            is PrintScriptV1SemanticError.InvalidUnaryOperand -> "InvalidUnaryOperand"
            is PrintScriptV1SemanticError.DivisionByZero -> "DivisionByZero"
            is PrintScriptV1SemanticError.UnsupportedBinaryOperator -> "UnsupportedBinaryOperator"
        }
    }

    private fun caseNameOf(error: PrintScriptV1LexicalError): String {
        return when (error) {
            is PrintScriptV1LexicalError.UnterminatedString -> "UnterminatedString"
            is PrintScriptV1LexicalError.InvalidNumber -> "InvalidNumber"
        }
    }

    private fun caseNameOf(diagnostic: PrintScriptV1Diagnostic): String {
        return when (diagnostic) {
            is PrintScriptV1Diagnostic.NamingConventionViolation -> "NamingConventionViolation"
            is PrintScriptV1Diagnostic.UnsupportedPrintlnArgument -> "UnsupportedPrintlnArgument"
        }
    }

    private fun describeLexical(error: LexicalError): String {
        return errorReporter.describe(ParseError.TokenRead(error))
    }

    private fun tokenOf(type: PrintScriptV1TokenType, lexeme: String): Token {
        return Token(type = type, lexeme = lexeme, span = anySpan)
    }

    private data class ExtensionLexicalError(
        override val span: SourceSpan,
    ) : LexicalError

    private data class ExtensionParseError(
        override val span: SourceSpan,
    ) : ParseError

    private data class ExtensionSemanticError(
        override val span: SourceSpan,
    ) : SemanticError

    private data class ExtensionFormattingError(
        override val span: SourceSpan,
    ) : FormattingError

    private data class ExtensionDiagnostic(
        override val span: SourceSpan,
    ) : Diagnostic
}
