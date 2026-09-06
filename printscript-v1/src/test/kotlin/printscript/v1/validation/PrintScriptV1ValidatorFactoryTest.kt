package printscript.v1.validation

import printscript.ast.DeclaredType
import printscript.interpreter.SemanticError
import printscript.statement.StatementReadResult
import printscript.statement.StatementSource
import printscript.v1.interpreter.PrintScriptV1SemanticError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class PrintScriptV1ValidatorFactoryTest {

    @Test
    fun `accepts arithmetic grouping concatenation and later initialization`() {
        val source = """
            let total: number;
            total = -(1 + 2) * 2 / 1;
            let message: string = "Total: " + total;
            println(message);
        """.trimIndent()

        assertEquals(ValidationResult.Success, validateV1(source))
        assertEquals(ValidationResult.Success, validateV1(""))
    }

    @Test
    fun `does not compute values to validate arithmetic`() {
        assertEquals(ValidationResult.Success, validateV1("println(1 / 0);"))
    }

    @Test
    fun `rejects incompatible declaration and assignment types`() {
        listOf(
            "let value: number = \"text\";",
            "let value: number; value = \"text\";",
        ).forEach { source ->
            val error = validateV1(source).semanticError<PrintScriptV1SemanticError.TypeMismatch>()
            assertEquals(DeclaredType.NUMBER, error.expected)
            assertEquals(DeclaredType.STRING, error.actual)
        }
    }

    @Test
    fun `rejects undeclared reads and assignments`() {
        listOf("println(missing);", "missing = 1;").forEach { source ->
            val error = validateV1(source).semanticError<PrintScriptV1SemanticError.UndeclaredVariable>()
            assertEquals("missing", error.name)
        }
    }

    @Test
    fun `requires initialization before a read`() {
        listOf(
            "let value: number; println(value);",
            "let value: number; value = value + 1;",
        ).forEach { source ->
            validateV1(source).semanticError<PrintScriptV1SemanticError.UninitializedVariable>()
        }
    }

    @Test
    fun `rejects duplicate declarations in the same scope`() {
        validateV1("let value: number; let value: string;")
            .semanticError<PrintScriptV1SemanticError.AlreadyDeclaredVariable>()
    }

    @Test
    fun `rejects invalid binary and unary operands`() {
        validateV1("println(\"text\" * 2);").semanticError<PrintScriptV1SemanticError.InvalidBinaryOperands>()
        validateV1("println(-\"text\");").semanticError<PrintScriptV1SemanticError.InvalidUnaryOperand>()
    }

    @Test
    fun `reports the position of a semantic error`() {
        val error = validateV1("let value: number = 1;\nprintln(missing);")
            .semanticError<PrintScriptV1SemanticError.UndeclaredVariable>()

        assertEquals(2, error.span.start.line)
        assertEquals("missing".length, error.span.end.column - error.span.start.column)
    }

    @Test
    fun `propagates syntax and lexical failures`() {
        listOf("let value: number = 1", "println(@);").forEach { source ->
            assertIs<ValidationResult.ParseFailure>(validateV1(source))
        }
    }

    @Test
    fun `does not enable version 1_1 expressions or statements on an externally supplied ast`() {
        val validator = PrintScriptV1ValidatorFactory.create()
        listOf("println(true);", "println(readInput(\"n\"));", "println(readEnv(\"N\"));").forEach { source ->
            validator.validate(statementsFrom(source)).semanticError<SemanticError.UnsupportedExpression>()
        }
        validator.validate(statementsFrom("let active: boolean;")).semanticError<SemanticError.UnsupportedStatement>()
        validator.validate(statementsFrom("if (active) {}"))
            .semanticError<SemanticError.UnsupportedStatement>()
    }

    @Test
    fun `reusing a validator does not retain declarations from another program`() {
        val validator = PrintScriptV1ValidatorFactory.create()

        assertEquals(ValidationResult.Success, validator.validate(statementsFrom("let value: number = 1;")))
        validator.validate(statementsFrom("println(value);"))
            .semanticError<PrintScriptV1SemanticError.UndeclaredVariable>()
    }

    @Test
    fun `stops pulling statements after the first semantic failure`() {
        val invalid = assertIs<StatementReadResult.Success>(statementsFrom("println(missing);").nextStatement())
        val source = object : StatementSource {
            override fun nextStatement(): StatementReadResult = invalid.copy(
                remainingSource = object : StatementSource {
                    override fun nextStatement(): StatementReadResult = error("Unexpected read after failure")
                },
            )
        }

        PrintScriptV1ValidatorFactory.create().validate(source)
            .semanticError<PrintScriptV1SemanticError.UndeclaredVariable>()
    }
}
