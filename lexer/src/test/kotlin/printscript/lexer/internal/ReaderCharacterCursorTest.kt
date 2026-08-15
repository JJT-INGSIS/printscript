package printscript.lexer.internal

import printscript.lexer.TrackingReader
import printscript.lexer.cursorFor
import printscript.model.source.SourcePosition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ReaderCharacterCursorTest {

    @Test
    fun `cursor starts at initial source position`() {
        val cursor = cursorFor("abc")

        assertCursorPosition(
            cursor = cursor,
            expectedLine = 1,
            expectedColumn = 1,
            expectedOffset = 0,
        )
    }

    @Test
    fun `peek returns next character without consuming or rereading it`() {
        val inputReader = TrackingReader("a")
        val cursor = ReaderCharacterCursor(inputReader)

        assertEquals('a', cursor.peek())
        assertEquals('a', cursor.peek())
        assertEquals(1, inputReader.readCalls)

        assertCursorPosition(
            cursor = cursor,
            expectedLine = 1,
            expectedColumn = 1,
            expectedOffset = 0,
        )
    }

    @Test
    fun `advance consumes next character and updates position`() {
        val cursor = cursorFor("ab")

        assertEquals('a', cursor.advance())

        assertCursorPosition(
            cursor = cursor,
            expectedLine = 1,
            expectedColumn = 2,
            expectedOffset = 1,
        )

        assertEquals('b', cursor.advance())

        assertCursorPosition(
            cursor = cursor,
            expectedLine = 1,
            expectedColumn = 3,
            expectedOffset = 2,
        )
    }

    @Test
    fun `advance consumes stored lookahead without reading it again`() {
        val inputReader = TrackingReader("ab")
        val cursor = ReaderCharacterCursor(inputReader)

        assertEquals('a', cursor.peek())
        assertEquals(1, inputReader.readCalls)

        assertEquals('a', cursor.advance())
        assertEquals(1, inputReader.readCalls)

        assertEquals('b', cursor.peek())
        assertEquals(2, inputReader.readCalls)
    }

    @Test
    fun `end of input remains stable without additional reader reads`() {
        val inputReader = TrackingReader("")
        val cursor = ReaderCharacterCursor(inputReader)

        assertNull(cursor.peek())
        assertNull(cursor.peek())
        assertNull(cursor.advance())

        assertEquals(1, inputReader.readCalls)

        assertCursorPosition(
            cursor = cursor,
            expectedLine = 1,
            expectedColumn = 1,
            expectedOffset = 0,
        )
    }

    @Test
    fun `line feed moves cursor to beginning of next line`() {
        val cursor = cursorFor("a\nb")

        assertEquals('a', cursor.advance())
        assertEquals('\n', cursor.advance())

        assertCursorPosition(
            cursor = cursor,
            expectedLine = 2,
            expectedColumn = 1,
            expectedOffset = 2,
        )

        assertEquals('b', cursor.advance())

        assertCursorPosition(
            cursor = cursor,
            expectedLine = 2,
            expectedColumn = 2,
            expectedOffset = 3,
        )
    }

    @Test
    fun `CRLF counts as one logical line break`() {
        val cursor = cursorFor("\r\nx")

        assertEquals('\r', cursor.advance())

        assertCursorPosition(
            cursor = cursor,
            expectedLine = 2,
            expectedColumn = 1,
            expectedOffset = 1,
        )

        assertEquals('\n', cursor.advance())

        assertCursorPosition(
            cursor = cursor,
            expectedLine = 2,
            expectedColumn = 1,
            expectedOffset = 2,
        )

        assertEquals('x', cursor.advance())

        assertCursorPosition(
            cursor = cursor,
            expectedLine = 2,
            expectedColumn = 2,
            expectedOffset = 3,
        )
    }

    @Test
    fun `standalone carriage return moves cursor to beginning of next line`() {
        val cursor = cursorFor("\rx")

        assertEquals('\r', cursor.advance())

        assertCursorPosition(
            cursor = cursor,
            expectedLine = 2,
            expectedColumn = 1,
            expectedOffset = 1,
        )

        assertEquals('x', cursor.advance())

        assertCursorPosition(
            cursor = cursor,
            expectedLine = 2,
            expectedColumn = 2,
            expectedOffset = 2,
        )
    }

    private fun assertCursorPosition(
        cursor: ReaderCharacterCursor,
        expectedLine: Int,
        expectedColumn: Int,
        expectedOffset: Long,
    ) {
        assertEquals(
            expected = SourcePosition(
                line = expectedLine,
                column = expectedColumn,
                offset = expectedOffset,
            ),
            actual = cursor.position,
        )
    }
}