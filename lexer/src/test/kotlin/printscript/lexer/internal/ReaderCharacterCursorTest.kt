package printscript.lexer.internal

import printscript.lexer.TrackingReader
import printscript.lexer.cursorFor
import printscript.model.source.SourcePosition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ReaderCharacterCursorTest {

    @Test
    fun `starts at first source position`() {
        val cursor = cursorFor("abc")

        assertEquals(
            SourcePosition(
                line = 1,
                column = 1,
                offset = 0,
            ),
            cursor.position,
        )
    }

    @Test
    fun `peek does not consume character`() {
        val reader = TrackingReader("a")
        val cursor = ReaderCharacterCursor(reader)

        assertEquals('a', cursor.peek())
        assertEquals('a', cursor.peek())
        assertEquals(1, reader.readCalls)

        assertEquals(
            SourcePosition(1, 1, 0),
            cursor.position,
        )
    }

    @Test
    fun `advance consumes character and updates position`() {
        val cursor = cursorFor("ab")

        assertEquals('a', cursor.advance())
        assertEquals(
            SourcePosition(1, 2, 1),
            cursor.position,
        )

        assertEquals('b', cursor.advance())
        assertEquals(
            SourcePosition(1, 3, 2),
            cursor.position,
        )
    }

    @Test
    fun `EOF remains stable`() {
        val reader = TrackingReader("")
        val cursor = ReaderCharacterCursor(reader)

        assertNull(cursor.peek())
        assertNull(cursor.peek())
        assertNull(cursor.advance())

        assertEquals(1, reader.readCalls)
        assertEquals(
            SourcePosition(1, 1, 0),
            cursor.position,
        )
    }

    @Test
    fun `line feed advances to next line`() {
        val cursor = cursorFor("a\nb")

        cursor.advance()
        cursor.advance()

        assertEquals(
            SourcePosition(2, 1, 2),
            cursor.position,
        )

        assertEquals('b', cursor.advance())
        assertEquals(
            SourcePosition(2, 2, 3),
            cursor.position,
        )
    }

    @Test
    fun `CRLF represents one logical line break`() {
        val cursor = cursorFor("\r\nx")

        cursor.advance()
        assertEquals(
            SourcePosition(2, 1, 1),
            cursor.position,
        )

        cursor.advance()
        assertEquals(
            SourcePosition(2, 1, 2),
            cursor.position,
        )

        assertEquals('x', cursor.advance())
        assertEquals(
            SourcePosition(2, 2, 3),
            cursor.position,
        )
    }

    @Test
    fun `lone carriage return advances line`() {
        val cursor = cursorFor("\rx")

        cursor.advance()

        assertEquals(
            SourcePosition(2, 1, 1),
            cursor.position,
        )

        assertEquals('x', cursor.advance())
        assertEquals(
            SourcePosition(2, 2, 2),
            cursor.position,
        )
    }
}