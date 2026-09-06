package printscript.ast.statement

import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan
import printscript.statement.Statement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

private val anySpan = SourceSpan(
    start = SourcePosition.initial(),
    end = SourcePosition.initial(),
)

private data class FakeStatement(override val span: SourceSpan = anySpan) : Statement

class BlockStatementTest {

    @Test
    fun `is unaffected by mutating the list passed to its constructor`() {
        val original = mutableListOf<Statement>(FakeStatement())
        val block = BlockStatement(statements = original, span = anySpan)

        original.add(FakeStatement())

        assertEquals(expected = 1, actual = block.statements.size)
    }

    @Test
    fun `rejects mutation through a cast to MutableList`() {
        val block = BlockStatement(statements = listOf(FakeStatement()), span = anySpan)

        @Suppress("UNCHECKED_CAST")
        val mutableView = block.statements as MutableList<Statement>

        assertFailsWith<UnsupportedOperationException> {
            mutableView.add(FakeStatement())
        }
    }
}
