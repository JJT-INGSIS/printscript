package printscript.source.internal

import printscript.source.SourceChunkReadResult
import printscript.source.SourceReader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class StringSourceReaderTest {

    @Test
    fun `reads source code in consecutive chunks`() {
        val reader: SourceReader = StringSourceReader(
            sourceCode = "abcdefgh",
            nextOffset = 0,
            chunkSize = 3,
        )

        val firstRead = assertIs<SourceChunkReadResult.Success>(
            reader.readChunk(),
        )
        assertEquals("abc", firstRead.chunk.content)

        val secondRead = assertIs<SourceChunkReadResult.Success>(
            firstRead.remainingReader.readChunk(),
        )
        assertEquals("def", secondRead.chunk.content)

        val thirdRead = assertIs<SourceChunkReadResult.Success>(
            secondRead.remainingReader.readChunk(),
        )
        assertEquals("gh", thirdRead.chunk.content)

        assertIs<SourceChunkReadResult.EndOfInput>(
            thirdRead.remainingReader.readChunk(),
        )
    }

    @Test
    fun `reading does not modify source reader`() {
        val reader: SourceReader = StringSourceReader(
            sourceCode = "source",
            nextOffset = 0,
            chunkSize = 3,
        )

        val firstRead = reader.readChunk()
        val repeatedRead = reader.readChunk()

        assertEquals(
            expected = firstRead,
            actual = repeatedRead,
        )
    }

    @Test
    fun `empty source immediately reaches end of input`() {
        val reader: SourceReader = StringSourceReader(
            sourceCode = "",
            nextOffset = 0,
            chunkSize = 3,
        )

        assertIs<SourceChunkReadResult.EndOfInput>(
            reader.readChunk(),
        )
    }

    @Test
    fun `factory creates reader from source code`() {
        val reader = printscript.source.SourceReaderFactory
            .fromString("let")

        val result = assertIs<SourceChunkReadResult.Success>(
            reader.readChunk(),
        )

        assertEquals(
            expected = "let",
            actual = result.chunk.content,
        )
    }
}