package printscript.e2e

import printscript.interpreter.InterpretationResult
import printscript.runtime.EnvironmentVariableProvider
import printscript.runtime.ProgramInput
import printscript.runtime.ProgramOutput
import printscript.source.SourceReaderCreationResult
import printscript.source.SourceReaderFactory
import printscript.statement.StatementSource
import printscript.v1.interpreter.PrintScriptV11InterpreterFactory
import printscript.v1.interpreter.PrintScriptV1SemanticError
import printscript.v1.lexer.PrintScriptV11LexerFactory
import printscript.v1.parser.PrintScriptV11ParserFactory
import printscript.v1.validation.PrintScriptV11ValidatorFactory
import printscript.v1.validation.ValidationResult
import java.io.ByteArrayInputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class PrintScriptV11ValidationEndToEndTest {

    @Test
    fun `validation and execution agree on nested shadows with different types`() {
        val source = """
            const value: number = 1;
            let active: boolean = true;
            if (active) {
                let value: string = "niño";
                value = "café";
                if (active) { const value: boolean = false; println(value); }
                println(value);
            }
            println(value);
        """.trimIndent()

        assertEquals(ValidationResult.Success, validate(source))
        val execution = execute(source)
        assertEquals(InterpretationResult.Success, execution.result)
        assertEquals(listOf("false", "café", "1"), execution.lines)
    }

    @Test
    fun `reassignments target the nearest binding and preserve the outer variable`() {
        val source = """
            let value: number = 1;
            let active: boolean = true;
            if (active) {
                let value: number = 2;
                if (active) { value = 1; }
                println(value);
            }
            println(value);
            if (active) { value = 2; }
            println(value);
        """.trimIndent()

        assertEquals(ValidationResult.Success, validate(source))
        val execution = execute(source)
        assertEquals(InterpretationResult.Success, execution.result)
        assertEquals(listOf("1", "1", "2"), execution.lines)
    }

    @Test
    fun `a local constant cannot be reassigned even when the outer binding is mutable`() {
        val source = """
            let value: number = 1;
            let active: boolean = true;
            if (active) { const value: number = 2; value = 1; }
        """.trimIndent()

        val validation = assertIs<ValidationResult.SemanticFailure>(validate(source))
        val execution = assertIs<InterpretationResult.SemanticFailure>(execute(source).result)
        assertIs<PrintScriptV1SemanticError.ConstantReassignment>(validation.error)
        assertEquals(validation.error, execution.error)
    }

    @Test
    fun `both operations reject duplicates in the same scope`() {
        val source = "let active: boolean = true; if (active) { let value: number; let value: string; }"

        val validation = assertIs<ValidationResult.SemanticFailure>(validate(source))
        val execution = assertIs<InterpretationResult.SemanticFailure>(execute(source).result)
        assertIs<PrintScriptV1SemanticError.AlreadyDeclaredVariable>(validation.error)
        assertEquals(validation.error, execution.error)
    }

    @Test
    fun `validation checks an unexecuted branch without changing execution behavior`() {
        val source = "let active: boolean = true; if (active) { println(1); } else { println(missing); }"

        val failure = assertIs<ValidationResult.SemanticFailure>(validate(source))
        assertIs<PrintScriptV1SemanticError.UndeclaredVariable>(failure.error)
        val execution = execute(source)
        assertEquals(InterpretationResult.Success, execution.result)
        assertEquals(listOf("1"), execution.lines)
    }

    @Test
    fun `input conversion errors remain execution errors`() {
        val source = "let count: number = readInput(\"Cantidad\"); println(count);"

        assertEquals(ValidationResult.Success, validate(source))
        val execution = execute(source, input = ProgramInput { "not a number" })
        val failure = assertIs<InterpretationResult.SemanticFailure>(execution.result)
        assertIs<PrintScriptV1SemanticError.InvalidInputValue>(failure.error)
        assertEquals(emptyList(), execution.lines)
    }

    @Test
    fun `missing environment variables remain execution errors`() {
        val source = "let count: number = readEnv(\"MISSING\");"

        assertEquals(ValidationResult.Success, validate(source))
        val execution = execute(source, environmentVariables = EnvironmentVariableProvider { null })
        val failure = assertIs<InterpretationResult.SemanticFailure>(execution.result)
        assertIs<PrintScriptV1SemanticError.EnvironmentVariableNotFound>(failure.error)
    }

    @Test
    fun `execution keeps the string result of reads inside println`() {
        listOf("readInput(\"n\")", "readEnv(\"N\")").forEach { read ->
            val source = "println($read * 2);"
            val validation = assertIs<ValidationResult.SemanticFailure>(validate(source))
            val execution = execute(
                source,
                input = ProgramInput { "2" },
                environmentVariables = EnvironmentVariableProvider { "2" },
            )
            val failure = assertIs<InterpretationResult.SemanticFailure>(execution.result)
            assertIs<PrintScriptV1SemanticError.InvalidBinaryOperands>(validation.error)
            assertEquals(validation.error, failure.error)
        }
    }

    @Test
    fun `validation and execution agree on binary operand types`() {
        val operands = listOf("2", "\"text\"", "true")
        val operators = listOf("+", "-", "*", "/")
        val expressions = operands.flatMap { left ->
            operators.flatMap { operator ->
                operands.map { right -> "println($left $operator $right);" }
            }
        }

        expressions.forEach { source ->
            val validation = validate(source)
            val execution = execute(source).result
            when (validation) {
                ValidationResult.Success -> assertEquals(InterpretationResult.Success, execution, source)
                is ValidationResult.SemanticFailure -> {
                    val failure = assertIs<InterpretationResult.SemanticFailure>(execution, source)
                    assertEquals(validation.error, failure.error, source)
                }

                is ValidationResult.ParseFailure -> error("Unexpected parsing failure in $source")
            }
        }
    }

    private fun validate(source: String): ValidationResult {
        return PrintScriptV11ValidatorFactory.create().validate(statementsFromStream(source))
    }

    private fun execute(
        source: String,
        input: ProgramInput = ProgramInput { error("Unexpected program input") },
        environmentVariables: EnvironmentVariableProvider =
            EnvironmentVariableProvider { error("Unexpected environment lookup") },
    ): Execution {
        val lines = mutableListOf<String>()
        val output = object : ProgramOutput {
            override fun writeLine(line: String) {
                lines.add(line)
            }
        }
        val interpreter = PrintScriptV11InterpreterFactory.create(output, input, environmentVariables)
        return Execution(interpreter.interpret(statementsFromStream(source)), lines.toList())
    }

    private fun statementsFromStream(source: String): StatementSource {
        val reader = assertIs<SourceReaderCreationResult.Success>(
            SourceReaderFactory.fromInputStream(
                ByteArrayInputStream(source.toByteArray(Charsets.UTF_8)),
                bufferSizeInCharacters = 1,
            ),
        ).reader
        return PrintScriptV11ParserFactory.create().parse(PrintScriptV11LexerFactory.create().tokenize(reader))
    }

    private data class Execution(val result: InterpretationResult, val lines: List<String>)
}
