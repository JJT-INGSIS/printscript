package printscript.e2e

import printscript.interpreter.InterpretationResult
import printscript.runtime.EnvironmentVariableProvider
import printscript.runtime.ProgramInput
import printscript.v1.interpreter.PrintScriptV1SemanticError
import printscript.v1.validation.ValidationResult
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

        assertEquals(expected = ValidationResult.Success, actual = validate(source))

        val execution = execute(source)

        assertEquals(expected = InterpretationResult.Success, actual = execution.result)
        assertEquals(expected = listOf("false", "café", "1"), actual = execution.outputLines)
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

        assertEquals(expected = ValidationResult.Success, actual = validate(source))

        val execution = execute(source)

        assertEquals(expected = InterpretationResult.Success, actual = execution.result)
        assertEquals(expected = listOf("1", "1", "2"), actual = execution.outputLines)
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
        assertEquals(expected = validation.error, actual = execution.error)
    }

    @Test
    fun `both operations reject duplicates in the same scope`() {
        val source = "let active: boolean = true; if (active) { let value: number; let value: string; }"

        val validation = assertIs<ValidationResult.SemanticFailure>(validate(source))
        val execution = assertIs<InterpretationResult.SemanticFailure>(execute(source).result)

        assertIs<PrintScriptV1SemanticError.AlreadyDeclaredVariable>(validation.error)
        assertEquals(expected = validation.error, actual = execution.error)
    }

    @Test
    fun `validation checks an unexecuted branch without changing execution behavior`() {
        val source = "let active: boolean = true; if (active) { println(1); } else { println(missing); }"

        val failure = assertIs<ValidationResult.SemanticFailure>(validate(source))
        assertIs<PrintScriptV1SemanticError.UndeclaredVariable>(failure.error)

        val execution = execute(source)

        assertEquals(expected = InterpretationResult.Success, actual = execution.result)
        assertEquals(expected = listOf("1"), actual = execution.outputLines)
    }

    @Test
    fun `input conversion errors remain execution errors`() {
        val source = """let count: number = readInput("Cantidad"); println(count);"""

        assertEquals(expected = ValidationResult.Success, actual = validate(source))

        val execution = execute(source, input = ProgramInput { "not a number" })
        val failure = assertIs<InterpretationResult.SemanticFailure>(execution.result)

        assertIs<PrintScriptV1SemanticError.InvalidInputValue>(failure.error)
        assertEquals(expected = emptyList(), actual = execution.outputLines)
    }

    @Test
    fun `missing environment variables remain execution errors`() {
        val source = """let count: number = readEnv("MISSING");"""

        assertEquals(expected = ValidationResult.Success, actual = validate(source))

        val execution = execute(source, environmentVariables = EnvironmentVariableProvider { null })
        val failure = assertIs<InterpretationResult.SemanticFailure>(execution.result)

        assertIs<PrintScriptV1SemanticError.EnvironmentVariableNotFound>(failure.error)
    }

    @Test
    fun `reads inside println stay strings and cannot be multiplied`() {
        listOf("""readInput("n")""", """readEnv("N")""").forEach { read ->
            val source = "println($read * 2);"

            val validation = assertIs<ValidationResult.SemanticFailure>(validate(source))
            val execution = execute(
                source,
                input = ProgramInput { "2" },
                environmentVariables = EnvironmentVariableProvider { "2" },
            )
            val failure = assertIs<InterpretationResult.SemanticFailure>(execution.result)

            assertIs<PrintScriptV1SemanticError.InvalidBinaryOperands>(validation.error)
            assertEquals(expected = validation.error, actual = failure.error)
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
                ValidationResult.Success ->
                    assertEquals(expected = InterpretationResult.Success, actual = execution, message = source)

                is ValidationResult.SemanticFailure -> {
                    val failure = assertIs<InterpretationResult.SemanticFailure>(execution, source)
                    assertEquals(expected = validation.error, actual = failure.error, message = source)
                }

                is ValidationResult.ParseFailure -> error("Fallo de parsing inesperado en $source")
            }
        }
    }

    private fun validate(source: String): ValidationResult {
        return validateV11ScriptFromStream(
            sourceCode = source,
            bufferSizeInCharacters = SINGLE_CHARACTER_BUFFER,
        )
    }

    private fun execute(
        source: String,
        input: ProgramInput = unexpectedProgramInput(),
        environmentVariables: EnvironmentVariableProvider = unexpectedEnvironmentVariables(),
    ): ProgramExecution {
        return runV11ScriptFromStream(
            sourceCode = source,
            bufferSizeInCharacters = SINGLE_CHARACTER_BUFFER,
            input = input,
            environmentVariables = environmentVariables,
        )
    }
}
