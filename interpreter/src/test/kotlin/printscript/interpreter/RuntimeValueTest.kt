package printscript.interpreter

import printscript.interpreter.value.NumberValue
import printscript.interpreter.value.StringValue
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals

class RuntimeValueTest {

    @Test
    fun `un entero se imprime sin decimales`() {
        val value = NumberValue(BigDecimal("3.0"))
        assertEquals("3", value.asText())
    }

    @Test
    fun `un decimal conserva sus digitos`() {
        val value = NumberValue(BigDecimal("3.5"))
        assertEquals("3.5", value.asText())
    }

    @Test
    fun `un string se imprime tal cual`() {
        val value = StringValue("hola")
        assertEquals("hola", value.asText())
    }

    @Test
    fun `un numero es de tipo number`() {
        val value = NumberValue(BigDecimal("1"))
        assertEquals(PrintScriptType.NUMBER, value.type)
    }

    @Test
    fun `un string es de tipo string`() {
        val value = StringValue("x")
        assertEquals(PrintScriptType.STRING, value.type)
    }
}