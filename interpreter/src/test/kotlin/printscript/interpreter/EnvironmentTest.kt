package printscript.interpreter

import printscript.ast.DeclaredType
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
    fun `an undeclared variable does not exist`() {
        val environment = MapEnvironment()
        assertNull(environment.lookup("x"))
    }

    @Test
    fun `a variable can be declared without initializing it`() {
        val environment = MapEnvironment()
        environment.declare("x", VariableBinding(DeclaredType.NUMBER, null))

        val binding = environment.lookup("x")

        assertNotNull(binding)
        assertEquals(DeclaredType.NUMBER, binding.type)
        assertNull(binding.value)
    }

    @Test
    fun `updating preserves the declared type`() {
        val environment = MapEnvironment()
        environment.declare("x", VariableBinding(DeclaredType.NUMBER, null))

        environment.update("x", NumberValue(BigDecimal("5")))

        val binding = environment.lookup("x")

        assertNotNull(binding)
        assertEquals(DeclaredType.NUMBER, binding.type)
        assertEquals(NumberValue(BigDecimal("5")), binding.value)
    }
}