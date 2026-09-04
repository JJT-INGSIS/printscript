package printscript.runtime

import printscript.ast.DeclaredType
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

    private val fiveValue = NumberValue(BigDecimal("5"))

    private val five =
        VariableBinding(
            type = DeclaredType.NUMBER,
            value = fiveValue,
        )

    @Test
    fun `an undeclared variable does not exist`() {
        assertNull(EnvironmentFactory.empty().lookupBinding("x"))
    }

    @Test
    fun `a variable can be added without initializing it`() {
        val environment = EnvironmentFactory.empty()
            .declaring("x", uninitializedNumber)

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
        val original = EnvironmentFactory.empty()

        original.declaring("x", uninitializedNumber)

        assertNull(original.lookupBinding("x"))
    }

    @Test
    fun `replacing a binding does not affect the previous environment`() {
        val declared = EnvironmentFactory.empty()
            .declaring("x", uninitializedNumber)

        val initialized = declared
            .reassigning("x", fiveValue)

        assertNull(declared.lookupBinding("x")?.value)

        assertEquals(
            expected = NumberValue(BigDecimal("5")),
            actual = initialized.lookupBinding("x")?.value,
        )
    }

    @Test
    fun `a binding declared in a nested scope disappears after leaving it`() {
        val outer = EnvironmentFactory.empty()
        val inner = outer
            .enteringScope()
            .declaring("local", five)

        assertEquals(expected = five, actual = inner.lookupBinding("local"))
        assertNull(inner.leavingScope().lookupBinding("local"))
        assertNull(outer.lookupBinding("local"))
    }

    @Test
    fun `reassigning an outer binding from a nested scope survives after leaving it`() {
        val outer = EnvironmentFactory.empty()
            .declaring("x", uninitializedNumber)
        val reassigned = outer
            .enteringScope()
            .reassigning("x", fiveValue)
            .leavingScope()

        assertEquals(expected = five, actual = reassigned.lookupBinding("x"))
        assertEquals(expected = uninitializedNumber, actual = outer.lookupBinding("x"))
    }

    @Test
    fun `the nearest declaration is found first`() {
        val outerValue = VariableBinding(
            type = DeclaredType.NUMBER,
            value = NumberValue(BigDecimal.ONE),
        )
        val environment = EnvironmentFactory.empty()
            .declaring("x", outerValue)
            .enteringScope()
            .declaring("x", five)

        assertEquals(expected = five, actual = environment.lookupBinding("x"))
        assertEquals(expected = outerValue, actual = environment.leavingScope().lookupBinding("x"))
    }

    @Test
    fun `reassigning preserves the binding metadata`() {
        val constant = VariableBinding(
            type = DeclaredType.NUMBER,
            value = NumberValue(BigDecimal.ONE),
            reassignable = false,
        )
        val environment = EnvironmentFactory.empty()
            .declaring("answer", constant)
            .reassigning("answer", fiveValue)

        assertEquals(
            expected = constant.copy(value = fiveValue),
            actual = environment.lookupBinding("answer"),
        )
    }
}
