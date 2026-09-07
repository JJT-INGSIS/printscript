package printscript.e2e

import printscript.interpreter.InterpretationResult
import printscript.runtime.ProgramInput
import printscript.statement.ParseError
import printscript.token.LexicalError
import printscript.v1.interpreter.PrintScriptV1SemanticError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class PrintScriptPipelineBoundaryTest {

    @Test
    fun `runs a full V1 program read one character at a time`() {
        val execution = runV1ScriptFromStream(
            sourceCode = """
                let name: string = "Joe";
                let count: number = 2 + 3 * 4;
                println("hello " + name);
                println(count);
            """.trimIndent(),
            bufferSizeInCharacters = SINGLE_CHARACTER_BUFFER,
        )

        assertEquals(expected = InterpretationResult.Success, actual = execution.result)
        assertEquals(expected = listOf("hello Joe", "14"), actual = execution.outputLines)
    }

    @Test
    fun `preserves multi-byte UTF-8 content split across buffer boundaries`() {
        val execution = runV1ScriptFromStream(
            sourceCode = """println("Aá€😀ñ");""",
            bufferSizeInCharacters = SINGLE_CHARACTER_BUFFER,
        )

        assertEquals(expected = InterpretationResult.Success, actual = execution.result)
        assertEquals(expected = listOf("Aá€😀ñ"), actual = execution.outputLines)
    }

    @Test
    fun `propagates a lexical error found through a small buffer`() {
        val execution = runV1ScriptFromStream(
            sourceCode = "println(@);",
            bufferSizeInCharacters = TWO_CHARACTER_BUFFER,
        )

        val failure = assertIs<InterpretationResult.ParseFailure>(execution.result)
        val parseError = assertIs<ParseError.TokenRead>(failure.error)
        val lexicalError = assertIs<LexicalError.UnexpectedCharacter>(parseError.error)

        assertEquals(expected = '@', actual = lexicalError.character)
        assertEquals(expected = emptyList(), actual = execution.outputLines)
    }

    @Test
    fun `propagates a semantic error found through a small buffer`() {
        val execution = runV1ScriptFromStream(
            sourceCode = "println(missingVariable);",
            bufferSizeInCharacters = TWO_CHARACTER_BUFFER,
        )

        val failure = assertIs<InterpretationResult.SemanticFailure>(execution.result)
        val semanticError = assertIs<PrintScriptV1SemanticError.UndeclaredVariable>(failure.error)

        assertEquals(expected = "missingVariable", actual = semanticError.name)
    }

    @Test
    fun `runs a V1_1 program with readInput through a small buffer`() {
        val execution = runV11ScriptFromStream(
            sourceCode = """
                let name: string = readInput("Nombre: ");
                println("hi " + name);
            """.trimIndent(),
            bufferSizeInCharacters = SINGLE_CHARACTER_BUFFER,
            input = ProgramInput { "Ana" },
        )

        assertEquals(expected = InterpretationResult.Success, actual = execution.result)
        assertEquals(expected = listOf("hi Ana"), actual = execution.outputLines)
    }

    @Test
    fun `runs a V1_1 program with an if block through a small buffer`() {
        val execution = runV11ScriptFromStream(
            sourceCode = """
                let active: boolean = true;
                if (active) {
                    println("yes");
                } else {
                    println("no");
                }
            """.trimIndent(),
            bufferSizeInCharacters = SINGLE_CHARACTER_BUFFER,
        )

        assertEquals(expected = InterpretationResult.Success, actual = execution.result)
        assertEquals(expected = listOf("yes"), actual = execution.outputLines)
    }
}
