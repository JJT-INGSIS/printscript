package printscript.interpreter

import printscript.ast.DeclaredType
import printscript.interpreter.value.NumberValue
import printscript.interpreter.value.StringValue
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals

class RuntimeValueTest {

    @Test
    fun `an integer is printed without decimals`() {
        val value = NumberValue(BigDecimal("3.0"))
        assertEquals("3", value.asText())
    }

    @Test
    fun `a decimal keeps its digits`() {
        val value = NumberValue(BigDecimal("3.5"))
        assertEquals("3.5", value.asText())
    }

    @Test
    fun `a string is printed as is`() {
        val value = StringValue("hola")
        assertEquals("hola", value.asText())
    }

    @Test
    fun `a number value has number type`() {
        val value = NumberValue(BigDecimal("1"))
        assertEquals(DeclaredType.NUMBER, value.type)
    }

    @Test
    fun `a string value has string type`() {
        val value = StringValue("x")
        assertEquals(DeclaredType.STRING, value.type)
    }
}