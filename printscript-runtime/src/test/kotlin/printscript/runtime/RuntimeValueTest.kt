package printscript.runtime

import printscript.ast.DeclaredType
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

    @Test
    fun `a true boolean is printed as true`() {
        val value = BooleanValue(true)
        assertEquals("true", value.asText())
    }

    @Test
    fun `a false boolean is printed as false`() {
        val value = BooleanValue(false)
        assertEquals("false", value.asText())
    }

    @Test
    fun `a boolean value has boolean type`() {
        val value = BooleanValue(true)
        assertEquals(DeclaredType.BOOLEAN, value.type)
    }
}
