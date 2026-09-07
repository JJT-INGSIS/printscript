package printscript.cli

import com.github.ajalt.clikt.testing.test
import printscript.cli.internal.command.ExecutionCommand
import printscript.cli.internal.report.ErrorReporter
import printscript.runtime.EnvironmentVariableProvider
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class ExecutionCliTest {

    @Test
    fun `executes a valid program and prints its output`() {
        val file = scriptFile(
            """
            let name: string = "Joe";
            println(name + " Doe");
            """.trimIndent(),
        )

        val result = cli().test(listOf("execution", file))

        assertEquals(expected = 0, actual = result.statusCode)
        assertContains(result.stdout, "Joe Doe")
    }

    @Test
    fun `evaluates arithmetic respecting precedence`() {
        val file = scriptFile("println(2 + 3 * 4);")

        assertContains(cli().test(listOf("execution", file)).stdout, "14")
    }

    @Test
    fun `executes a version 1_1 conditional`() {
        val file = scriptFile(
            """
            let active: boolean = true;
            if (active) {
                println("enabled");
            }
            """.trimIndent(),
        )

        val result = cli().test(listOf("execution", file, "--version", "1.1"))

        assertEquals(expected = 0, actual = result.statusCode)
        assertContains(result.stdout, "enabled")
    }

    @Test
    fun `reads program input through the terminal in version 1_1`() {
        val file = scriptFile(
            """
            let value: number = readInput("Number: ");
            println(value + 1);
            """.trimIndent(),
        )

        val result = cli().test(
            argv = listOf("execution", file, "--version", "1.1"),
            stdin = "4\n",
        )

        assertEquals(expected = 0, actual = result.statusCode)
        assertContains(result.stdout, "Number: ")
        assertContains(result.stdout, "5")
    }

    @Test
    fun `reads environment variables through the cli boundary in version 1_1`() {
        val file = scriptFile(
            """
            let port: number = readEnv("PORT");
            println(port + 1);
            """.trimIndent(),
        )
        val command = ExecutionCommand(
            errorReporter = ErrorReporter(),
            environmentVariables = EnvironmentVariableProvider { name ->
                if (name == "PORT") "4" else null
            },
        )

        val result = command.test(listOf(file, "--version", "1.1"))

        assertEquals(expected = 0, actual = result.statusCode)
        assertContains(result.stdout, "5")
    }

    @Test
    fun `reports division by zero`() {
        val file = scriptFile("println(1 / 0);")

        val result = cli().test(listOf("execution", file))

        assertEquals(expected = 1, actual = result.statusCode)
        assertContains(result.stderr, "división por cero")
    }
}
