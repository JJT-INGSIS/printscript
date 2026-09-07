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
    fun `validates reads without requesting stdin or printing prompts`() {
        val source = """
            let count: number = readInput("NEVER_SHOW_THIS_PROMPT");
            let active: boolean = readEnv("PRINTSCRIPT_VALIDATION_ONLY_FLAG");
            if (active) { println(count); }
        """.trimIndent()

        val result = cli().test(listOf("validation", scriptFile(source), "--version", "1.1"))

        assertEquals(expected = 0, actual = result.statusCode)
        assertEquals(expected = "El archivo es válido.\n", actual = result.stdout)
        assertEquals(expected = "", actual = result.stderr)
    }

    @Test
    fun `rejects an undeclared variable`() {
        val file = scriptFile("println(inexistente);")

        val result = cli().test(listOf("validation", file))

        assertEquals(expected = 1, actual = result.statusCode)
        assertContains(result.stderr, "'inexistente' no fue declarada")
    }

    @Test
    fun `reports semantic errors from the unexecuted branch with their position`() {
        val source = "let active: boolean = true;\nif (active) {} else { println(missing); }"

        val result = cli().test(listOf("validation", scriptFile(source), "--version", "1.1"))

        assertEquals(expected = 1, actual = result.statusCode)
        assertContains(result.stderr, "'missing' no fue declarada")
        assertContains(result.stderr, "línea 2")
        assertEquals(expected = "", actual = result.stdout)
    }

    @Test
    fun `reports a syntax error with its position`() {
        val file = scriptFile("let a: number = 5")

        val result = cli().test(listOf("validation", file))

        assertEquals(expected = 1, actual = result.statusCode)
        assertContains(result.stderr, "línea 1")
    }

    @Test
    fun `reports parsing errors without executing preceding statements`() {
        val source = "println(\"NEVER_PRINT\");\nlet value: number = ;"

        val result = cli().test(listOf("validation", scriptFile(source)))

        assertEquals(expected = 1, actual = result.statusCode)
        assertContains(result.stderr, "línea 2")
        assertEquals(expected = "", actual = result.stdout)
    }

    @Test
    fun `runtime arithmetic errors do not fail static validation`() {
        val file = scriptFile("println(1 / 0);")

        assertEquals(expected = 0, actual = cli().test(listOf("validation", file)).statusCode)
        assertEquals(expected = 1, actual = cli().test(listOf("execution", file)).statusCode)
    }

    @Test
    fun `checks version specific syntax`() {
        val file = scriptFile("const active: boolean = true; if (active) { println(active); }")

        assertEquals(
            expected = 1,
            actual = cli().test(listOf("validation", file, "--version", "1.0")).statusCode,
        )
        assertEquals(
            expected = 0,
            actual = cli().test(listOf("validation", file, "--version", "1.1")).statusCode,
        )
    }
}
