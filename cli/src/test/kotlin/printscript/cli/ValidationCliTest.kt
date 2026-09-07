package printscript.cli

import com.github.ajalt.clikt.testing.test
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class ValidationCliTest {

    @Test
    fun `accepts a well formed program`() {
        val file = scriptFile("let a: number = 5;")

        val result = cli().test(listOf("validation", file))

        assertEquals(expected = 0, actual = result.statusCode)
        assertContains(result.stdout, "El archivo es válido.")
    }

    @Test
    fun `does not print what the program would print`() {
        val file = scriptFile("""println("no debería verse");""")

        val result = cli().test(listOf("validation", file))

        assertFalse(result.stdout.contains("no debería verse"))
    }

    @Test
    fun `rejects an undeclared variable`() {
        val file = scriptFile("println(inexistente);")

        val result = cli().test(listOf("validation", file))

        assertEquals(expected = 1, actual = result.statusCode)
        assertContains(result.stderr, "'inexistente' no fue declarada")
    }

    @Test
    fun `reports a syntax error with its position`() {
        val file = scriptFile("let a: number = 5")

        val result = cli().test(listOf("validation", file))

        assertEquals(expected = 1, actual = result.statusCode)
        assertContains(result.stderr, "línea 1")
    }
}
