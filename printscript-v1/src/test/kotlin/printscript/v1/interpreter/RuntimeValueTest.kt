package printscript.v1.interpreter

import printscript.ast.DeclaredType
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals

class RuntimeValueTest {

    @Test
    fun `an integer is printed without decimals`() {
        val value = PrintScriptV1NumberValue(BigDecimal("3.0"))
        assertEquals("3", value.asText())
    }

    @Test
    fun `a decimal keeps its digits`() {
        val value = PrintScriptV1NumberValue(BigDecimal("3.5"))
        assertEquals("3.5", value.asText())
    }

    @Test
    fun `a string is printed as is`() {
        val value = PrintScriptV1StringValue("hola")
        assertEquals("hola", value.asText())
    }

    @Test
    fun `a number value has number type`() {
        val value = PrintScriptV1NumberValue(BigDecimal("1"))
        assertEquals(DeclaredType.NUMBER, value.type)
    }

    @Test
    fun `a string value has string type`() {
        val value = PrintScriptV1StringValue("x")
        assertEquals(DeclaredType.STRING, value.type)
    }
}
