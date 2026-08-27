package printscript.e2e

import printscript.interpreter.InterpretationResult
import printscript.interpreter.SemanticError
import printscript.statement.ParseError
import printscript.token.LexicalError
import printscript.v1.token.PrintScriptV1TokenType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class PrintScriptV1EndToEndTest {

    @Test
    fun `executes string declarations and concatenation`() {
        val execution = runV1Script(
            sourceCode = """
                let name: string = "Joe";
                let lastName: string = "Doe";
                println(name + " " + lastName);
            """.trimIndent(),
        )

        assertSuccessfulExecution(
            execution = execution,
            expectedOutputLines = listOf("Joe Doe"),
        )
    }

    @Test
    fun `respects arithmetic operator precedence`() {
        val execution = runV1Script(
            sourceCode = """
                let result: number = 2 + 3 * 4;
                println(result);
            """.trimIndent(),
        )

        assertSuccessfulExecution(
            execution = execution,
            expectedOutputLines = listOf("14"),
        )
    }

    @Test
    fun `evaluates parenthesized expressions`() {
        val execution = runV1Script(
            sourceCode = """
                let result: number = (2 + 3) * 4;
                println(result);
            """.trimIndent(),
        )

        assertSuccessfulExecution(
            execution = execution,
            expectedOutputLines = listOf("20"),
        )
    }

    @Test
    fun `uses previously declared variables in expressions`() {
        val execution = runV1Script(
            sourceCode = """
                let first: number = 10;
                let second: number = 5;
                let result: number = first + second;
                println(result);
            """.trimIndent(),
        )

        assertSuccessfulExecution(
            execution = execution,
            expectedOutputLines = listOf("15"),
        )
    }

    @Test
    fun `executes assignment and mixed concatenation`() {
        val execution = runV1Script(
            sourceCode = """
                let value: number = 12;
                value = value / 4;
                println("Result: " + value);
            """.trimIndent(),
        )

        assertSuccessfulExecution(
            execution = execution,
            expectedOutputLines = listOf("Result: 3"),
        )
    }

    @Test
    fun `preserves decimal results`() {
        val execution = runV1Script(
            sourceCode = """
                let result: number = 7 / 2;
                println(result);
            """.trimIndent(),
        )

        assertSuccessfulExecution(
            execution = execution,
            expectedOutputLines = listOf("3.5"),
        )
    }

    @Test
    fun `executes multiple output statements in order`() {
        val execution = runV1Script(
            sourceCode = """
                println("first");
                println("second");
                println("third");
            """.trimIndent(),
        )

        assertSuccessfulExecution(
            execution = execution,
            expectedOutputLines = listOf(
                "first",
                "second",
                "third",
            ),
        )
    }

    @Test
    fun `propagates lexical errors through the complete pipeline`() {
        val execution = runV1Script(
            sourceCode = """
                println(@);
            """.trimIndent(),
        )

        val interpretationFailure =
            assertIs<InterpretationResult.ParseFailure>(
                execution.result,
            )

        val parseError =
            assertIs<ParseError.Lexical>(
                interpretationFailure.error,
            )

        val lexicalError =
            assertIs<LexicalError.UnexpectedCharacter>(
                parseError.error,
            )

        assertEquals(
            expected = '@',
            actual = lexicalError.character,
        )

        assertNoOutputWasProduced(execution)
    }

    @Test
    fun `propagates syntax errors through the complete pipeline`() {
        val execution = runV1Script(
            sourceCode = """
                println(1)
            """.trimIndent(),
        )

        val interpretationFailure =
            assertIs<InterpretationResult.ParseFailure>(
                execution.result,
            )

        val parseError =
            assertIs<ParseError.UnexpectedToken>(
                interpretationFailure.error,
            )

        assertEquals(
            expected = setOf(PrintScriptV1TokenType.SEMICOLON),
            actual = parseError.expected,
        )

        assertEquals(
            expected = PrintScriptV1TokenType.EOF,
            actual = parseError.actual.type,
        )

        assertNoOutputWasProduced(execution)
    }

    @Test
    fun `propagates semantic errors through the complete pipeline`() {
        val execution = runV1Script(
            sourceCode = """
                println(missingVariable);
            """.trimIndent(),
        )

        val interpretationFailure =
            assertIs<InterpretationResult.SemanticFailure>(
                execution.result,
            )

        val semanticError =
            assertIs<SemanticError.UndeclaredVariable>(
                interpretationFailure.error,
            )

        assertEquals(
            expected = "missingVariable",
            actual = semanticError.name,
        )

        assertNoOutputWasProduced(execution)
    }

    @Test
    fun `evaluates subtraction and division from left to right`() {
        val execution = runV1Script(
            """
        let result: number = 20 / 2 - 3;
        println(result);
            """.trimIndent(),
        )

        assertSuccessfulExecution(
            execution,
            listOf("7"),
        )
    }

    @Test
    fun `uses latest assigned value`() {
        val execution = runV1Script(
            """
        let value: number = 10;
        value = 20;
        value = value + 5;
        println(value);
            """.trimIndent(),
        )

        assertSuccessfulExecution(
            execution,
            listOf("25"),
        )
    }

    private fun assertSuccessfulExecution(execution: ProgramExecution, expectedOutputLines: List<String>) {
        assertEquals(
            expected = InterpretationResult.Success,
            actual = execution.result,
        )

        assertEquals(
            expected = expectedOutputLines,
            actual = execution.outputLines,
        )
    }

    private fun assertNoOutputWasProduced(execution: ProgramExecution) {
        assertEquals(
            expected = emptyList<String>(),
            actual = execution.outputLines,
        )
    }
}
