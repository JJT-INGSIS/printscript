package printscript.interpreter

import kotlin.test.Test
import kotlin.test.assertEquals

class ProgramOutputTest {

    @Test
    fun `the in memory output keeps the lines in order`() {
        val output = InMemoryOutput()

        output.writeLine("uno")
        output.writeLine("dos")

        assertEquals(listOf("uno", "dos"), output.lines())
    }
}
