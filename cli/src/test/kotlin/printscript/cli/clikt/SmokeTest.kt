package printscript.cli.clikt

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.testing.test
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

/**
 * Verifica que Clikt esté en el classpath y que funcione todo lo que
 * necesitan las fases 3 a 5: subcomandos, argumento posicional, opción
 * con valor por defecto, salida separada a stdout y stderr, y la
 * traducción de [ProgramResult] a código de salida.
 *
 * No prueba PrintScript. Se borra en la Fase 6, cuando los subcomandos
 * de verdad cubran lo mismo.
 */
class SmokeTest {

    private class RootProbe : CliktCommand(name = "printscript") {

        override fun run() = Unit
    }

    private class OperationProbe : CliktCommand(name = "validation") {

        private val sourceFilePath: String by argument()

        private val version: String by option("--version").default("1.0")

        override fun run() {
            echo("archivo=$sourceFilePath version=$version")
            echo("progreso", err = true)

            throw ProgramResult(3)
        }
    }

    private fun probeCommand() = RootProbe().subcommands(OperationProbe())

    @Test
    fun `reads a positional argument and an option with a default`() {
        val result = probeCommand().test("validation ejemplo.ps")

        assertEquals(
            expected = "archivo=ejemplo.ps version=1.0\n",
            actual = result.stdout,
        )
    }

    @Test
    fun `reads an explicit option value`() {
        val result = probeCommand().test("validation ejemplo.ps --version 2.0")

        assertContains(result.stdout, "version=2.0")
    }

    @Test
    fun `keeps standard output and standard error separated`() {
        val result = probeCommand().test("validation ejemplo.ps")

        assertContains(result.stderr, "progreso")
        assertEquals(expected = false, actual = result.stdout.contains("progreso"))
    }

    @Test
    fun `translates ProgramResult into an exit code`() {
        assertEquals(
            expected = 3,
            actual = probeCommand().test("validation ejemplo.ps").statusCode,
        )
    }

    @Test
    fun `generates a help page listing the subcommands`() {
        val result = probeCommand().test("--help")

        assertContains(result.stdout, "validation")
    }

    @Test
    fun `reports a missing required argument without our code running`() {
        val result = probeCommand().test("validation")

        assertEquals(expected = false, actual = result.statusCode == 0)
        assertEquals(expected = "", actual = result.stdout)
    }
}
