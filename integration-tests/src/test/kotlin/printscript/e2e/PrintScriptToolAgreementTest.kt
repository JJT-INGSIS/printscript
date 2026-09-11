package printscript.e2e

import printscript.interpreter.InterpretationResult
import printscript.runtime.EnvironmentVariableProvider
import printscript.runtime.ProgramInput
import printscript.v1.formatter.configuration.EqualsSpacing
import printscript.v1.formatter.configuration.IfBracePlacement
import printscript.v1.formatter.configuration.PrintScriptV11FormatterConfiguration
import printscript.v1.formatter.configuration.PrintScriptV1FormatterConfiguration
import printscript.v1.linter.PrintScriptV11LinterConfiguration
import printscript.v1.linter.PrintScriptV11LinterConfigurationResult
import printscript.v1.linter.PrintScriptV11LinterFactory
import printscript.v1.linter.PrintScriptV1Diagnostic
import printscript.v1.linter.PrintScriptV1LinterFactory
import printscript.v1.validation.ValidationResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class PrintScriptToolAgreementTest {

    private val v1FormatterConfiguration = PrintScriptV1FormatterConfiguration(
        equalsSpacing = EqualsSpacing.SURROUNDED_BY_SPACES,
        enforceSpaceAfterColonInDeclaration = true,
        enforceSpaceAroundBinaryOperators = true,
        enforceSingleSpaceSeparation = true,
        enforceLineBreakAfterStatement = true,
    )

    private val v11FormatterConfiguration = PrintScriptV11FormatterConfiguration(
        v1Configuration = v1FormatterConfiguration,
        ifBracePlacement = IfBracePlacement.SAME_LINE,
        indentationInsideIf = 4,
    )

    @Test
    fun `formatting preserves what a V1 program prints`() {
        val source = """
            let name:string="Joe";
            let count:number=2+3*4;
            println("hello "+name);
            println(count);
        """.trimIndent()

        val formatted = formatV1(source)
        val original = runV1Script(source)
        val reformatted = runV1Script(formatted)

        assertEquals(expected = InterpretationResult.Success, actual = original.result)
        assertEquals(expected = InterpretationResult.Success, actual = reformatted.result)
        assertEquals(expected = listOf("hello Joe", "14"), actual = original.outputLines)
        assertEquals(expected = original.outputLines, actual = reformatted.outputLines)
    }

    @Test
    fun `formatting preserves what a V1_1 program prints`() {
        val source = """
            const greeting:string="hola";
            let active:boolean=true;
            if(active){println(greeting);}else{println("adiós");}
        """.trimIndent()

        val formatted = formatV11(source)
        val original = runV11Script(source)
        val reformatted = runV11Script(formatted)

        assertEquals(expected = InterpretationResult.Success, actual = original.result)
        assertEquals(expected = InterpretationResult.Success, actual = reformatted.result)
        assertEquals(expected = listOf("hola"), actual = original.outputLines)
        assertEquals(expected = original.outputLines, actual = reformatted.outputLines)
    }

    @Test
    fun `formatting preserves what the linter reports`() {
        val source = """
            let mi_variable:number=5;
            println(mi_variable);
        """.trimIndent()

        val formatted = formatV1(source)

        val beforeFormatting = assertIs<PrintScriptV1Diagnostic.NamingConventionViolation>(
            diagnosticsOf(source).single(),
        )
        val afterFormatting = assertIs<PrintScriptV1Diagnostic.NamingConventionViolation>(
            diagnosticsOf(formatted).single(),
        )

        assertEquals(expected = "mi_variable", actual = beforeFormatting.identifier.value)
        assertEquals(
            expected = beforeFormatting.identifier.value,
            actual = afterFormatting.identifier.value,
        )
    }

    @Test
    fun `every tool accepts the same V1_1 program`() {
        val source = """
            const greeting: string = "hola";
            let userName: string = readInput("Nombre: ");
            let port: number = readEnv("PORT");
            let active: boolean = true;
            if (active) {
                println(greeting + " " + userName);
            } else {
                println("adiós");
            }
            println(port + 1);
        """.trimIndent()

        assertIs<ProgramFormatting.Success>(
            formatV11ScriptFromStream(
                sourceCode = source,
                bufferSizeInCharacters = SINGLE_CHARACTER_BUFFER,
                configuration = v11FormatterConfiguration,
            ),
        )

        val analysis = lintV11ScriptFromStream(
            sourceCode = source,
            bufferSizeInCharacters = SINGLE_CHARACTER_BUFFER,
            configuration = camelCaseConfiguration(),
        )

        assertEquals(
            expected = emptyList(),
            actual = assertIs<ProgramAnalysis.Success>(analysis).diagnostics,
        )
        assertEquals(expected = ValidationResult.Success, actual = validateV11Script(source))

        val execution = runV11Script(
            sourceCode = source,
            input = ProgramInput { "Juan" },
            environmentVariables = EnvironmentVariableProvider { "8080" },
        )

        assertEquals(expected = InterpretationResult.Success, actual = execution.result)
        assertEquals(expected = listOf("hola Juan", "8081"), actual = execution.outputLines)
    }

    private fun formatV1(source: String): String {
        return assertIs<ProgramFormatting.Success>(
            formatV1ScriptFromStream(
                sourceCode = source,
                bufferSizeInCharacters = SINGLE_CHARACTER_BUFFER,
                configuration = v1FormatterConfiguration,
            ),
        ).formattedText
    }

    private fun formatV11(source: String): String {
        return assertIs<ProgramFormatting.Success>(
            formatV11ScriptFromStream(
                sourceCode = source,
                bufferSizeInCharacters = SINGLE_CHARACTER_BUFFER,
                configuration = v11FormatterConfiguration,
            ),
        ).formattedText
    }

    private fun diagnosticsOf(source: String) = assertIs<ProgramAnalysis.Success>(
        lintV1ScriptFromStream(
            sourceCode = source,
            bufferSizeInCharacters = SINGLE_CHARACTER_BUFFER,
            configuration = PrintScriptV1LinterFactory.defaultConfiguration(),
        ),
    ).diagnostics

    private fun camelCaseConfiguration(): PrintScriptV11LinterConfiguration {
        return assertIs<PrintScriptV11LinterConfigurationResult.Success>(
            PrintScriptV11LinterFactory.configurationFrom("""{"identifier_format": "camel case"}"""),
        ).configuration
    }
}
