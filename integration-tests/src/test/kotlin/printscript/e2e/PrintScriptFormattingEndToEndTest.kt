package printscript.e2e

import printscript.formatter.FormattingError
import printscript.token.LexicalError
import printscript.v1.formatter.EqualsSpacing
import printscript.v1.formatter.PrintScriptV11FormatterConfiguration
import printscript.v1.formatter.PrintScriptV1FormatterConfiguration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class PrintScriptFormattingEndToEndTest {

    @Test
    fun `formats a V1 program read one character at a time`() {
        val formatting = formatV1ScriptFromStream(
            sourceCode = """let name:string="Joe";println(name);""",
            bufferSizeInCharacters = SINGLE_CHARACTER_BUFFER,
            configuration = PrintScriptV1FormatterConfiguration(
                equalsSpacing = EqualsSpacing.SURROUNDED_BY_SPACES,
                enforceSpaceAfterColonInDeclaration = true,
                enforceLineBreakAfterStatement = true,
            ),
        )

        val formatted = assertIs<ProgramFormatting.Success>(formatting)

        assertEquals(
            expected = "let name: string = \"Joe\";\nprintln(name);",
            actual = formatted.formattedText,
        )
    }

    @Test
    fun `indents a V1_1 if block read one character at a time`() {
        val formatting = formatV11ScriptFromStream(
            sourceCode = """
                if (active) {
                println("yes");
                }
            """.trimIndent(),
            bufferSizeInCharacters = SINGLE_CHARACTER_BUFFER,
            configuration = PrintScriptV11FormatterConfiguration(indentationInsideIf = 2u),
        )

        val formatted = assertIs<ProgramFormatting.Success>(formatting)

        assertEquals(
            expected = "if (active) {\n  println(\"yes\");\n}",
            actual = formatted.formattedText,
        )
    }

    @Test
    fun `propagates a lexical error found by the formatting lexer`() {
        val formatting = formatV1ScriptFromStream(
            sourceCode = "println(@);",
            bufferSizeInCharacters = TWO_CHARACTER_BUFFER,
            configuration = PrintScriptV1FormatterConfiguration(),
        )

        val failure = assertIs<ProgramFormatting.Failure>(formatting)
        val tokenReadFailure = assertIs<FormattingError.TokenReadFailure>(failure.error)
        val lexicalError = assertIs<LexicalError.UnexpectedCharacter>(tokenReadFailure.tokenReadError)

        assertEquals(expected = '@', actual = lexicalError.character)
    }
}
