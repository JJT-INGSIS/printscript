package printscript.cli

import com.github.ajalt.clikt.testing.test
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class FormattingCliTest {

    @Test
    fun `preserves source gaps when no rules are configured`() {
        val source = "let value :number= 1;"
        val file = scriptFile(source)

        val result = cli().test(listOf("formatting", file))

        assertEquals(expected = 0, actual = result.statusCode)
        assertEquals(expected = source, actual = result.stdout)
    }

    @Test
    fun `reports lexical errors from its lossless token stream`() {
        val file = scriptFile("let value: number = @;")

        val result = cli().test(listOf("formatting", file))

        assertEquals(expected = 1, actual = result.statusCode)
        assertContains(result.stderr, "el carácter '@' no pertenece al lenguaje")
    }

    @Test
    fun `uses the selected version and its json configuration`() {
        val file = scriptFile("if (true) {\n}")
        val configuration = configurationFile("""{"if-brace-below-line": true}""")

        val result = cli().test(
            listOf("formatting", file, "--version", "1.1", "--config", configuration),
        )

        assertEquals(expected = 0, actual = result.statusCode)
        assertEquals(expected = "if (true)\n{\n}", actual = result.stdout)
    }

    @Test
    fun `reports an invalid json configuration`() {
        val file = scriptFile("println(1);")
        val configuration = configurationFile("not json")

        val result = cli().test(listOf("formatting", file, "--config", configuration))

        assertEquals(expected = 1, actual = result.statusCode)
        assertContains(result.stderr, "no es un documento JSON válido")
    }

    @Test
    fun `reports excessive configured line breaks`() {
        val file = scriptFile("println(1);")
        val configuration = configurationFile("""{"line-breaks-after-println": ${Int.MAX_VALUE}}""")

        val result = cli().test(listOf("formatting", file, "--config", configuration))

        assertEquals(expected = 1, actual = result.statusCode)
        assertContains(result.stderr, "excede el máximo admitido")
        assertEquals(expected = "", actual = result.stdout)
    }

    @Test
    fun `reports indentation overflow with its source position`() {
        val file = scriptFile("if(a){if(b){\nprintln(1);}}")
        val configuration = configurationFile("""{"indent-inside-if": ${Int.MAX_VALUE}}""")

        val result = cli().test(
            listOf("formatting", file, "--version", "1.1", "--config", configuration),
        )

        assertEquals(expected = 1, actual = result.statusCode)
        assertContains(result.stderr, "cantidad de espacios o saltos de línea excede el tamaño admitido")
        assertContains(result.stderr, "línea 2")
    }
}
