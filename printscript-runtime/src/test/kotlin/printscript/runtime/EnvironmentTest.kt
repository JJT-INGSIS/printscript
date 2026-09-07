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
        assertNull(EnvironmentFactory.empty().findBinding("x"))
    }

    @Test
    fun `current scope lookup excludes outer declarations`() {
        val outer = EnvironmentFactory.empty().declare("x", five)
        val inner = outer.enterScope()

        assertEquals(five, outer.findBindingInCurrentScope("x"))
        assertNull(inner.findBindingInCurrentScope("x"))
        assertEquals(five, inner.findBinding("x"))
    }

    @Test
    fun `reassigning a shadow leaves the outer binding untouched`() {
        val outer = EnvironmentFactory.empty().declare("x", uninitializedNumber)
        val inner = outer.enterScope().declare("x", uninitializedNumber)
        val reassigned = inner.reassign("x", fiveValue)

        assertEquals(five, reassigned.findBindingInCurrentScope("x"))
        assertEquals(uninitializedNumber, reassigned.leaveScope().findBinding("x"))
        assertEquals(uninitializedNumber, inner.findBinding("x"))
    }

    @Test
    fun `a variable can be added without initializing it`() {
        val environment = EnvironmentFactory.empty()
            .declare("x", uninitializedNumber)

        val binding = environment.findBinding("x")

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

        original.declare("x", uninitializedNumber)

        assertNull(original.findBinding("x"))
    }

    @Test
    fun `replacing a binding does not affect the previous environment`() {
        val declared = EnvironmentFactory.empty()
            .declare("x", uninitializedNumber)

        val initialized = declared
            .reassign("x", fiveValue)

        assertNull(declared.findBinding("x")?.value)

        assertEquals(
            expected = NumberValue(BigDecimal("5")),
            actual = initialized.findBinding("x")?.value,
        )
    }

    @Test
    fun `a binding declared in a nested scope disappears after leaving it`() {
        val outer = EnvironmentFactory.empty()
        val inner = outer
            .enterScope()
            .declare("local", five)

        assertEquals(expected = five, actual = inner.findBinding("local"))
        assertNull(inner.leaveScope().findBinding("local"))
        assertNull(outer.findBinding("local"))
    }

    @Test
    fun `reassigning an outer binding from a nested scope survives after leaving it`() {
        val outer = EnvironmentFactory.empty()
            .declare("x", uninitializedNumber)
        val reassigned = outer
            .enterScope()
            .reassign("x", fiveValue)
            .leaveScope()

        assertEquals(expected = five, actual = reassigned.findBinding("x"))
        assertEquals(expected = uninitializedNumber, actual = outer.findBinding("x"))
    }

    @Test
    fun `the nearest declaration is found first`() {
        val outerValue = VariableBinding(
            type = DeclaredType.NUMBER,
            value = NumberValue(BigDecimal.ONE),
        )
        val environment = EnvironmentFactory.empty()
            .declare("x", outerValue)
            .enterScope()
            .declare("x", five)

        assertEquals(expected = five, actual = environment.findBinding("x"))
        assertEquals(expected = outerValue, actual = environment.leaveScope().findBinding("x"))
    }

    @Test
    fun `reassigning preserves the binding metadata`() {
        val constant = VariableBinding(
            type = DeclaredType.NUMBER,
            value = NumberValue(BigDecimal.ONE),
            reassignable = false,
        )
        val environment = EnvironmentFactory.empty()
            .declare("answer", constant)
            .reassign("answer", fiveValue)

        assertEquals(
            expected = constant.copy(value = fiveValue),
            actual = environment.findBinding("answer"),
        )
    }
}
