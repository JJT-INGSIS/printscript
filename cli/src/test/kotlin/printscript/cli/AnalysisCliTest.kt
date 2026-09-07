package printscript.cli

import com.github.ajalt.clikt.testing.test
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class AnalysisCliTest {

    @Test
    fun `accepts a program that respects the conventions`() {
        val file = scriptFile("let miVariable: number = 5;")

        val result = cli().test(listOf("analysis", file))

        assertEquals(expected = 0, actual = result.statusCode)
        assertContains(result.stdout, "No se encontraron problemas.")
    }

    @Test
    fun `reports an identifier that is not camel case`() {
        val file = scriptFile("let mi_variable: number = 5;")

        val result = cli().test(listOf("analysis", file))

        assertEquals(expected = 3, actual = result.statusCode)
        assertContains(result.stdout, "camelCase")
    }

    @Test
    fun `uses the selected version and its json configuration`() {
        val file = scriptFile("let mi_variable: number = 5;")
        val configuration = configurationFile("""{"identifier_format": "snake case"}""")

        val result = cli().test(
            listOf("analysis", file, "--version", "1.1", "--config", configuration),
        )

        assertEquals(expected = 0, actual = result.statusCode)
        assertContains(result.stdout, "No se encontraron problemas.")
    }

    @Test
    fun `reports version 1_1 diagnostics with their own wording`() {
        val file = scriptFile(
            """
            let name: string = "Joe";
            let answer: string = readInput("Hello " + name);
            """.trimIndent(),
        )
        val configuration = configurationFile(
            """{"mandatory-variable-or-literal-in-readInput": true}""",
        )

        val result = cli().test(
            listOf("analysis", file, "--version", "1.1", "--config", configuration),
        )

        assertEquals(expected = 3, actual = result.statusCode)
        assertContains(result.stdout, "readInput no acepta una expresión como mensaje")
    }

    @Test
    fun `a file with findings exits differently than a broken file`() {
        val withFindings = scriptFile("let mi_variable: number = 5;")
        val broken = scriptFile("let a: number = 5")

        assertEquals(
            expected = 3,
            actual = cli().test(listOf("analysis", withFindings)).statusCode,
        )
        assertEquals(
            expected = 1,
            actual = cli().test(listOf("analysis", broken)).statusCode,
        )
    }
}
