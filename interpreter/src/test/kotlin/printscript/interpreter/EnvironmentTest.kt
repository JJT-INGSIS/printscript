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

    private val uninitializedNumber =
        VariableBinding(
            type = DeclaredType.NUMBER,
            value = null,
        )

    private val five =
        VariableBinding(
            type = DeclaredType.NUMBER,
            value = NumberValue(BigDecimal("5")),
        )

    @Test
    fun `an undeclared variable does not exist`() {
        assertNull(MapEnvironment().lookupBinding("x"))
    }

    @Test
    fun `a variable can be added without initializing it`() {
        val environment = MapEnvironment()
            .withBinding("x", uninitializedNumber)

        val binding = environment.lookupBinding("x")

        assertNotNull(binding)

        assertEquals(
            expected = DeclaredType.NUMBER,
            actual = binding.type,
        )

        assertNull(binding.value)
    }

    @Test
    fun `adding a binding leaves the original environment untouched`() {
        val original = MapEnvironment()

        original.withBinding("x", uninitializedNumber)

        assertNull(original.lookupBinding("x"))
    }

    @Test
    fun `replacing a binding does not affect the previous environment`() {
        val declared = MapEnvironment()
            .withBinding("x", uninitializedNumber)

        val initialized = declared
            .withBinding("x", five)

        assertNull(declared.lookupBinding("x")?.value)

        assertEquals(
            expected = NumberValue(BigDecimal("5")),
            actual = initialized.lookupBinding("x")?.value,
        )
    }
}
