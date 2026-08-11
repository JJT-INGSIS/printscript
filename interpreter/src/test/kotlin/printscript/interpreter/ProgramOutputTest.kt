package printscript.interpreter

import printscript.interpreter.InMemoryOutput
import kotlin.test.Test
import kotlin.test.assertEquals

class ProgramOutputTest {

    @Test
    fun `the in memory output keeps the lines in order`() {
        val output = InMemoryOutput()

        output.emit("uno")
        output.emit("dos")

        assertEquals(listOf("uno", "dos"), output.lines())
    }
}