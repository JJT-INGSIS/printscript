package printscript.cli

import printscript.cli.internal.arguments.ArgumentsParsingResult
import printscript.cli.internal.arguments.CliArgumentsParser
import printscript.cli.internal.operation.LanguageVersion
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class CliArgumentsParserTest {

    private val parser = CliArgumentsParser()

    private fun parseSuccessfully(vararg arguments: String) = assertIs<ArgumentsParsingResult.Success>(
        parser.parseArguments(arguments.toList()),
    )

    private fun parseFailure(vararg arguments: String) = assertIs<ArgumentsParsingResult.Failure>(
        parser.parseArguments(arguments.toList()),
    )

    @Test
    fun `reads the operation and the source file`() {
        val parsing = parseSuccessfully("validation", "archivo.ps")

        assertEquals(expected = "validation", actual = parsing.operationName)
        assertEquals(expected = Path.of("archivo.ps"), actual = parsing.request.sourceFilePath)
    }

    @Test
    fun `defaults to the only supported version`() {
        assertEquals(
            expected = LanguageVersion.V1_0,
            actual = parseSuccessfully("execution", "archivo.ps").request.version,
        )
    }

    @Test
    fun `accepts a configuration file without forcing an explicit version`() {
        val request = parseSuccessfully("formatting", "archivo.ps", "reglas.json").request

        assertEquals(expected = LanguageVersion.V1_0, actual = request.version)
        assertEquals(expected = "reglas.json", actual = request.configurationFilePath)
    }

    @Test
    fun `accepts version and configuration in any order`() {
        val first = parseSuccessfully("formatting", "archivo.ps", "1.0", "reglas.json")
        val second = parseSuccessfully("formatting", "archivo.ps", "reglas.json", "1.0")

        assertEquals(expected = first, actual = second)
    }

    @Test
    fun `has no configuration file when none is given`() {
        assertNull(parseSuccessfully("validation", "archivo.ps").request.configurationFilePath)
    }

    @Test
    fun `is case insensitive for the operation`() {
        assertEquals(
            expected = "execution",
            actual = parseSuccessfully("EXECUTION", "archivo.ps").operationName,
        )
    }

    @Test
    fun `rejects a call without a source file`() {
        parseFailure("validation")
    }

    @Test
    fun `rejects too many arguments`() {
        parseFailure("validation", "a.ps", "1.0", "r.json", "extra")
    }

    @Test
    fun `rejects a repeated version`() {
        parseFailure("validation", "a.ps", "1.0", "1.0")
    }
}
