package printscript.source.internal

import printscript.source.SourceAccessError
import printscript.source.SourceChunkReadResult
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame

class FileSourceReaderTest {

    @Test
    fun `successful reading returns the next immutable byte offset`() {
        val path = Files.createTempFile("printscript-reader", ".ps")
        path.toFile().deleteOnExit()
        Files.writeString(path, "áb")
        val reader = FileSourceReader(
            path = path,
            nextByteOffset = 0L,
            bufferSizeInBytes = 2,
        )

        val firstRead = assertIs<SourceChunkReadResult.Success>(reader.readChunk())
        val secondRead = assertIs<SourceChunkReadResult.Success>(firstRead.remainingReader.readChunk())

        assertEquals(expected = "á", actual = firstRead.chunk.content)
        assertEquals(expected = "b", actual = secondRead.chunk.content)
    }

    @Test
    fun `failure keeps the same reader available for retry`() {
        val path = Files.createTempFile("printscript-missing-reader", ".ps")
        Files.delete(path)
        val expectedError = SourceAccessError.NotFound(path)
        val reader = FileSourceReader(
            path = path,
            nextByteOffset = 5L,
            bufferSizeInBytes = 8,
        )

        val failure = assertIs<SourceChunkReadResult.Failure>(reader.readChunk())

        assertEquals(expected = expectedError, actual = failure.error)
        assertSame(expected = reader, actual = failure.remainingReader)
    }
}
