package printscript.lexer.internal

import printscript.lexer.assertEndOfInput
import printscript.lexer.assertNextCharacter
import printscript.lexer.cursorFor
import printscript.lexer.cursorForChunks
import printscript.model.source.SourcePosition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class CharacterCursorTest {

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
    fun `peek returns next character without consuming cursor`() {
        val cursor = cursorFor("a")

        val firstResult =
            assertIs<CharacterReadResult.Success>(
                cursor.peek(),
            )

        val secondResult =
            assertIs<CharacterReadResult.Success>(
                cursor.peek(),
            )

        assertEquals(
            expected = 'a',
            actual = firstResult.character,
        )

        assertEquals(
            expected = firstResult,
            actual = secondResult,
        )

        assertCursorPosition(
            cursor = cursor,
            expectedLine = 1,
            expectedColumn = 1,
            expectedOffset = 0,
        )
    }

    @Test
    fun `advance returns consumed character and resulting cursor`() {
        val initialCursor = cursorFor("ab")

        val firstResult =
            assertIs<CharacterReadResult.Success>(
                initialCursor.advance(),
            )

        assertEquals(
            expected = 'a',
            actual = firstResult.character,
        )

        assertCursorPosition(
            cursor = firstResult.resultingCursor,
            expectedLine = 1,
            expectedColumn = 2,
            expectedOffset = 1,
        )

        val secondResult =
            assertIs<CharacterReadResult.Success>(
                firstResult.resultingCursor.advance(),
            )

        assertEquals(
            expected = 'b',
            actual = secondResult.character,
        )

        assertCursorPosition(
            cursor = secondResult.resultingCursor,
            expectedLine = 1,
            expectedColumn = 3,
            expectedOffset = 2,
        )

        assertCursorPosition(
            cursor = initialCursor,
            expectedLine = 1,
            expectedColumn = 1,
            expectedOffset = 0,
        )
    }

    @Test
    fun `advance consumes character from cursor returned by peek`() {
        val cursor = cursorFor("ab")

        val peekResult =
            assertIs<CharacterReadResult.Success>(
                cursor.peek(),
            )

        val advanceResult =
            assertIs<CharacterReadResult.Success>(
                peekResult.resultingCursor.advance(),
            )

        assertEquals(
            expected = peekResult.character,
            actual = advanceResult.character,
        )

        advanceResult.resultingCursor.assertNextCharacter('b')
    }

    @Test
    fun `end of input remains stable`() {
        val cursor = cursorFor("")

        val firstResult =
            assertIs<CharacterReadResult.EndOfInput>(
                cursor.peek(),
            )

        firstResult.resultingCursor.assertEndOfInput()

        val advanceResult =
            assertIs<CharacterReadResult.EndOfInput>(
                firstResult.resultingCursor.advance(),
            )

        assertCursorPosition(
            cursor = advanceResult.resultingCursor,
            expectedLine = 1,
            expectedColumn = 1,
            expectedOffset = 0,
        )
    }

    @Test
    fun `line feed moves cursor to beginning of next line`() {
        val initialCursor = cursorFor("a\nb")

        val afterLetter = advance(
            cursor = initialCursor,
            expectedCharacter = 'a',
        )

        val afterLineFeed = advance(
            cursor = afterLetter,
            expectedCharacter = '\n',
        )

        assertCursorPosition(
            cursor = afterLineFeed,
            expectedLine = 2,
            expectedColumn = 1,
            expectedOffset = 2,
        )

        val afterFinalLetter = advance(
            cursor = afterLineFeed,
            expectedCharacter = 'b',
        )

        assertCursorPosition(
            cursor = afterFinalLetter,
            expectedLine = 2,
            expectedColumn = 2,
            expectedOffset = 3,
        )
    }

    @Test
    fun `CRLF counts as one logical line break`() {
        val initialCursor = cursorFor("\r\nx")

        val afterCarriageReturn = advance(
            cursor = initialCursor,
            expectedCharacter = '\r',
        )

        assertCursorPosition(
            cursor = afterCarriageReturn,
            expectedLine = 2,
            expectedColumn = 1,
            expectedOffset = 1,
        )

        val afterLineFeed = advance(
            cursor = afterCarriageReturn,
            expectedCharacter = '\n',
        )

        assertCursorPosition(
            cursor = afterLineFeed,
            expectedLine = 2,
            expectedColumn = 1,
            expectedOffset = 2,
        )

        val afterLetter = advance(
            cursor = afterLineFeed,
            expectedCharacter = 'x',
        )

        assertCursorPosition(
            cursor = afterLetter,
            expectedLine = 2,
            expectedColumn = 2,
            expectedOffset = 3,
        )
    }

    @Test
    fun `standalone carriage return moves cursor to beginning of next line`() {
        val initialCursor = cursorFor("\rx")

        val afterCarriageReturn = advance(
            cursor = initialCursor,
            expectedCharacter = '\r',
        )

        assertCursorPosition(
            cursor = afterCarriageReturn,
            expectedLine = 2,
            expectedColumn = 1,
            expectedOffset = 1,
        )

        val afterLetter = advance(
            cursor = afterCarriageReturn,
            expectedCharacter = 'x',
        )

        assertCursorPosition(
            cursor = afterLetter,
            expectedLine = 2,
            expectedColumn = 2,
            expectedOffset = 2,
        )
    }

    @Test
    fun `cursor continues across chunks and ignores empty chunks`() {
        val initialCursor = cursorForChunks(
            "a",
            "",
            "b",
        )

        val afterFirstCharacter = advance(
            cursor = initialCursor,
            expectedCharacter = 'a',
        )

        afterFirstCharacter.assertNextCharacter('b')

        val afterSecondCharacter = advance(
            cursor = afterFirstCharacter,
            expectedCharacter = 'b',
        )

        afterSecondCharacter.assertEndOfInput()
    }

    private fun advance(cursor: CharacterCursor, expectedCharacter: Char): CharacterCursor {
        val result = assertIs<CharacterReadResult.Success>(
            cursor.advance(),
        )

        assertEquals(
            expected = expectedCharacter,
            actual = result.character,
        )

        return result.resultingCursor
    }

    private fun assertCursorPosition(
        cursor: CharacterCursor,
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
