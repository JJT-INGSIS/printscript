package printscript.e2e

import printscript.statement.ParseError
import printscript.v1.linter.PrintScriptV11Diagnostic
import printscript.v1.linter.PrintScriptV11LinterConfigurationResult
import printscript.v1.linter.PrintScriptV11LinterFactory
import printscript.v1.linter.PrintScriptV1Diagnostic
import printscript.v1.linter.PrintScriptV1LinterFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class PrintScriptAnalysisEndToEndTest {

    @Test
    fun `reports a naming convention violation read one character at a time`() {
        val analysis = lintV1ScriptFromStream(
            sourceCode = "let mi_variable: number = 5;",
            bufferSizeInCharacters = SINGLE_CHARACTER_BUFFER,
            configuration = PrintScriptV1LinterFactory.defaultConfiguration(),
        )

        val diagnostics = assertIs<ProgramAnalysis.Success>(analysis).diagnostics
        val violation = assertIs<PrintScriptV1Diagnostic.NamingConventionViolation>(diagnostics.single())

        assertEquals(expected = "mi_variable", actual = violation.identifier.value)
    }

    @Test
    fun `accepts a program that respects the configured convention`() {
        val analysis = lintV1ScriptFromStream(
            sourceCode = "let miVariable: number = 5;",
            bufferSizeInCharacters = SINGLE_CHARACTER_BUFFER,
            configuration = PrintScriptV1LinterFactory.defaultConfiguration(),
        )

        val diagnostics = assertIs<ProgramAnalysis.Success>(analysis).diagnostics

        assertEquals(expected = emptyList(), actual = diagnostics)
    }

    @Test
    fun `propagates a parsing error instead of reporting diagnostics`() {
        val analysis = lintV1ScriptFromStream(
            sourceCode = "let a: number = 5",
            bufferSizeInCharacters = TWO_CHARACTER_BUFFER,
            configuration = PrintScriptV1LinterFactory.defaultConfiguration(),
        )

        val failure = assertIs<ProgramAnalysis.Failure>(analysis)

        assertIs<ParseError.UnexpectedToken>(failure.error)
    }

    @Test
    fun `reaches a readInput nested inside a V1_1 if block`() {
        val analysis = lintV11ScriptFromStream(
            sourceCode = """
                let active: boolean = true;
                if (active) {
                    let message: string = readInput("a" + "b");
                }
            """.trimIndent(),
            bufferSizeInCharacters = SINGLE_CHARACTER_BUFFER,
            configuration = readInputArgumentConfiguration(),
        )

        val diagnostics = assertIs<ProgramAnalysis.Success>(analysis).diagnostics

        assertIs<PrintScriptV11Diagnostic.UnsupportedReadInputArgument>(diagnostics.single())
    }

    private fun readInputArgumentConfiguration() = assertIs<PrintScriptV11LinterConfigurationResult.Success>(
        PrintScriptV11LinterFactory.configurationFrom(
            """{"mandatory-variable-or-literal-in-readInput": true}""",
        ),
    ).configuration
}
