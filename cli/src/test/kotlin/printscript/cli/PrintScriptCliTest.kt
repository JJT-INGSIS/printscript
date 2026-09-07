package printscript.cli

import com.github.ajalt.clikt.testing.test
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class PrintScriptCliTest {

    @Test
    fun `lists the four operations in the help page`() {
        val result = cli().test("--help")

        assertContains(result.stdout, "validation")
        assertContains(result.stdout, "execution")
        assertContains(result.stdout, "formatting")
        assertContains(result.stdout, "analysis")
    }

    @Test
    fun `rejects an unknown operation`() {
        val result = cli().test("dancing archivo.ps")

        assertNotEquals(illegal = 0, actual = result.statusCode)
    }

    @Test
    fun `rejects a call without a source file`() {
        val result = cli().test("validation")

        assertNotEquals(illegal = 0, actual = result.statusCode)
        assertEquals(expected = "", actual = result.stdout)
    }

    @Test
    fun `reports a missing source file with our own wording`() {
        val result = cli().test("validation /no/existe/archivo.ps")

        assertEquals(expected = 1, actual = result.statusCode)
        assertContains(result.stderr, "no se encontró el archivo")
    }

    @Test
    fun `reports invalid UTF-8 found while reading the source`() {
        val file = Files.createTempFile("printscript-invalid-utf8", ".ps")
        file.toFile().deleteOnExit()
        val validPrefix = "let a: number = 5;\n".toByteArray(Charsets.UTF_8)
        Files.write(file, validPrefix + byteArrayOf(0xC3.toByte()))

        val result = cli().test(listOf("validation", file.toString()))

        assertEquals(expected = 1, actual = result.statusCode)
        assertContains(result.stderr, "UTF-8")
        assertContains(result.stderr, "línea 2")
    }
}
