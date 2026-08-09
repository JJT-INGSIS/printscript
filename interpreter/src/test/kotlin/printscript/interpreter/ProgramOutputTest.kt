package printscript.interpreter

import printscript.interpreter.output.InMemoryOutput
import kotlin.test.Test
import kotlin.test.assertEquals

class ProgramOutputTest {

    @Test
    fun `la salida en memoria guarda las lineas en orden`() {
        val output = InMemoryOutput()

        output.emit("uno")
        output.emit("dos")

        assertEquals(listOf("uno", "dos"), output.lines())
    }
}