package printscript.interpreter

import printscript.interpreter.environment.MapEnvironment
import printscript.interpreter.environment.VariableBinding
import printscript.interpreter.value.NumberValue
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class EnvironmentTest {

    @Test
    fun `una variable no declarada no existe`() {
        val environment = MapEnvironment()
        assertNull(environment.lookup("x"))
    }

    @Test
    fun `se puede declarar sin inicializar`() {
        val environment = MapEnvironment()
        environment.declare("x", VariableBinding(PrintScriptType.NUMBER, null))

        val binding = environment.lookup("x")

        assertNotNull(binding)
        assertEquals(PrintScriptType.NUMBER, binding.type)
        assertNull(binding.value)
    }

    @Test
    fun `actualizar conserva el tipo declarado`() {
        val environment = MapEnvironment()
        environment.declare("x", VariableBinding(PrintScriptType.NUMBER, null))

        environment.update("x", NumberValue(BigDecimal("5")))

        val binding = environment.lookup("x")

        assertNotNull(binding)
        assertEquals(PrintScriptType.NUMBER, binding.type)
        assertEquals(NumberValue(BigDecimal("5")), binding.value)
    }
}