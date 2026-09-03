package printscript.cli

import com.github.ajalt.clikt.testing.test
import printscript.cli.internal.PrintScriptCommandFactory
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrintScriptCliTest {

    private fun printScriptCli() = PrintScriptCommandFactory.create()

    private fun scriptFile(sourceCode: String): String {
        val file = Files.createTempFile("printscript", ".ps")
        file.toFile().deleteOnExit()
        Files.writeString(file, sourceCode)

        return file.toString()
    }

    // --- execution -----------------------------------------------------

    @Test
    fun `executes a valid program and prints its output`() {
        val file = scriptFile(
            """
            let name: string = "Joe";
            println(name + " Doe");
            """.trimIndent(),
        )

        val result = printScriptCli().test(listOf("execution", file))

        assertEquals(expected = 0, actual = result.statusCode)
        assertContains(result.stdout, "Joe Doe")
    }

    @Test
    fun `evaluates arithmetic respecting precedence`() {
        val file = scriptFile("println(2 + 3 * 4);")

        assertContains(printScriptCli().test(listOf("execution", file)).stdout, "14")
    }

    @Test
    fun `reports division by zero`() {
        val file = scriptFile("println(1 / 0);")

        val result = printScriptCli().test(listOf("execution", file))

        assertEquals(expected = 1, actual = result.statusCode)
        assertContains(result.stderr, "división por cero")
    }

    // --- validation ----------------------------------------------------

    @Test
    fun `validation accepts a well formed program`() {
        val file = scriptFile("let a: number = 5;")

        val result = printScriptCli().test(listOf("validation", file))

        assertEquals(expected = 0, actual = result.statusCode)
        assertContains(result.stdout, "El archivo es válido.")
    }

    @Test
    fun `validation does not print what the program would print`() {
        val file = scriptFile("""println("no deberia verse");""")

        val result = printScriptCli().test(listOf("validation", file))

        assertTrue(!result.stdout.contains("no deberia verse"))
    }

    @Test
    fun `validation rejects an undeclared variable`() {
        val file = scriptFile("println(inexistente);")

        val result = printScriptCli().test(listOf("validation", file))

        assertEquals(expected = 1, actual = result.statusCode)
        assertContains(result.stderr, "'inexistente' no fue declarada")
    }

    @Test
    fun `reports a syntax error with its position`() {
        val file = scriptFile("let a: number = 5")

        val result = printScriptCli().test(listOf("validation", file))

        assertEquals(expected = 1, actual = result.statusCode)
        assertContains(result.stderr, "línea 1")
    }

    // --- formatting ----------------------------------------------------

    @Test
    fun `formatting preserves source gaps when no rules are configured`() {
        val source = "let value :number= 1;"
        val file = scriptFile(source)

        val result = printScriptCli().test(listOf("formatting", file))

        assertEquals(expected = 0, actual = result.statusCode)
        assertEquals(expected = source, actual = result.stdout)
    }

    @Test
    fun `formatting reports lexical errors from its lossless token stream`() {
        val file = scriptFile("let value: number = @;")

        val result = printScriptCli().test(listOf("formatting", file))

        assertEquals(expected = 1, actual = result.statusCode)
        assertContains(result.stderr, "el carácter '@' no pertenece al lenguaje")
    }

    // --- analysis ------------------------------------------------------

    @Test
    fun `accepts a program that respects the conventions`() {
        val file = scriptFile("let miVariable: number = 5;")

        val result = printScriptCli().test(listOf("analysis", file))

        assertEquals(expected = 0, actual = result.statusCode)
        assertContains(result.stdout, "No se encontraron problemas.")
    }

    @Test
    fun `reports an identifier that is not camel case`() {
        val file = scriptFile("let mi_variable: number = 5;")

        val result = printScriptCli().test(listOf("analysis", file))

        assertEquals(expected = 3, actual = result.statusCode)
        assertContains(result.stdout, "camelCase")
    }

    @Test
    fun `a file with findings exits differently than a broken file`() {
        val withFindings = scriptFile("let mi_variable: number = 5;")
        val broken = scriptFile("let a: number = 5")

        assertEquals(
            expected = 3,
            actual = printScriptCli().test(listOf("analysis", withFindings)).statusCode,
        )

        assertEquals(
            expected = 1,
            actual = printScriptCli().test(listOf("analysis", broken)).statusCode,
        )
    }

    // --- errores de uso, ahora los reporta Clikt ------------------------

    @Test
    fun `rejects an unknown operation`() {
        val result = printScriptCli().test("dancing archivo.ps")

        assertTrue(result.statusCode != 0)
    }

    @Test
    fun `rejects a call without a source file`() {
        val result = printScriptCli().test("validation")

        assertTrue(result.statusCode != 0)
        assertEquals(expected = "", actual = result.stdout)
    }

    @Test
    fun `reports a missing file with our own wording`() {
        val result = printScriptCli().test("validation /no/existe/archivo.ps")

        assertEquals(expected = 1, actual = result.statusCode)
        assertContains(result.stderr, "no se encontró el archivo")
    }

    @Test
    fun `reports invalid UTF-8 found while reading the source`() {
        val file = Files.createTempFile("printscript-invalid-utf8", ".ps")
        file.toFile().deleteOnExit()
        val validPrefix = "let a: number = 5;\n".toByteArray(Charsets.UTF_8)
        Files.write(file, validPrefix + byteArrayOf(0xC3.toByte()))

        val result = printScriptCli().test(listOf("validation", file.toString()))

        assertEquals(expected = 1, actual = result.statusCode)
        assertContains(result.stderr, "UTF-8")
        assertContains(result.stderr, "línea 2")
    }

    // --- ayuda ----------------------------------------------------------

    @Test
    fun `lists the four operations in the help page`() {
        val result = printScriptCli().test("--help")

        assertContains(result.stdout, "validation")
        assertContains(result.stdout, "execution")
        assertContains(result.stdout, "formatting")
        assertContains(result.stdout, "analysis")
    }
}
