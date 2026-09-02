package printscript.source

import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs

class InputStreamSourceReaderTest {

    @Test
    fun `reads the stream incrementally`() {
        val reader = readerFor(
            content = "abcdef",
            bufferSizeInCharacters = 3,
        )

        val firstRead = assertIs<SourceChunkReadResult.Success>(reader.readChunk())
        val secondRead = assertIs<SourceChunkReadResult.Success>(firstRead.remainingReader.readChunk())

        assertEquals(expected = "abc", actual = firstRead.chunk.content)
        assertEquals(expected = "def", actual = secondRead.chunk.content)
        assertIs<SourceChunkReadResult.EndOfInput>(secondRead.remainingReader.readChunk())
    }

    @Test
    fun `preserves UTF-8 characters read one byte at a time`() {
        val sourceCode = "Aá€😀B"
        val inputStream = OneByteAtATimeInputStream(
            sourceCode.toByteArray(StandardCharsets.UTF_8),
        )
        val reader = readerOf(
            SourceReaderFactory.fromInputStream(
                inputStream = inputStream,
                bufferSizeInCharacters = 1,
            ),
        )

        assertEquals(
            expected = sourceCode,
            actual = readAll(reader),
        )
    }

    @Test
    fun `does not consume the stream when the reader is created`() {
        val inputStream = ReadTrackingInputStream("println(1);")

        SourceReaderFactory.fromInputStream(inputStream)

        assertEquals(expected = 0, actual = inputStream.readCallCount)
    }

    @Test
    fun `does not close a stream owned by the caller`() {
        val inputStream = ReadTrackingInputStream("println(1);")
        val reader = readerOf(SourceReaderFactory.fromInputStream(inputStream))

        readAll(reader)

        assertFalse(inputStream.wasClosed)
    }

    @Test
    fun `reports invalid UTF-8`() {
        val inputStream = ByteArrayInputStream(
            byteArrayOf(0xC3.toByte()),
        )
        val reader = readerOf(SourceReaderFactory.fromInputStream(inputStream))

        val failure = assertIs<SourceChunkReadResult.Failure>(reader.readChunk())

        assertEquals(
            expected = SourceReadError.InvalidInputStreamEncoding,
            actual = failure.error,
        )
    }

    @Test
    fun `reports an input stream read failure`() {
        val reader = readerOf(
            SourceReaderFactory.fromInputStream(FailingInputStream),
        )

        val failure = assertIs<SourceChunkReadResult.Failure>(reader.readChunk())

        assertEquals(
            expected = SourceReadError.InputStreamReadFailed("stream disconnected"),
            actual = failure.error,
        )
    }

    @Test
    fun `rejects a zero character buffer`() {
        val creation = SourceReaderFactory.fromInputStream(
            inputStream = ByteArrayInputStream(byteArrayOf()),
            bufferSizeInCharacters = 0,
        )

        val failure = assertIs<SourceReaderCreationResult.Failure>(creation)

        assertEquals(
            expected = SourceReaderCreationError.InvalidBufferSize(0),
            actual = failure.error,
        )
    }

    @Test
    fun `rejects a negative character buffer`() {
        val creation = SourceReaderFactory.fromInputStream(
            inputStream = ByteArrayInputStream(byteArrayOf()),
            bufferSizeInCharacters = -1,
        )

        val failure = assertIs<SourceReaderCreationResult.Failure>(creation)

        assertEquals(
            expected = SourceReaderCreationError.InvalidBufferSize(-1),
            actual = failure.error,
        )
    }

    private fun readerFor(content: String, bufferSizeInCharacters: Int): SourceReader {
        return readerOf(
            SourceReaderFactory.fromInputStream(
                inputStream = ByteArrayInputStream(
                    content.toByteArray(StandardCharsets.UTF_8),
                ),
                bufferSizeInCharacters = bufferSizeInCharacters,
            ),
        )
    }

    private fun readerOf(creation: SourceReaderCreationResult): SourceReader {
        return assertIs<SourceReaderCreationResult.Success>(creation).reader
    }

    private tailrec fun readAll(reader: SourceReader, accumulated: String = ""): String {
        return when (val result = reader.readChunk()) {
            SourceChunkReadResult.EndOfInput -> accumulated

            is SourceChunkReadResult.Failure ->
                error("Unexpected source read failure: ${result.error}")

            is SourceChunkReadResult.Success ->
                readAll(
                    reader = result.remainingReader,
                    accumulated = accumulated + result.chunk.content,
                )
        }
    }

    private class ReadTrackingInputStream(content: String) : ByteArrayInputStream(
        content.toByteArray(StandardCharsets.UTF_8),
    ) {

        var readCallCount: Int = 0
            private set

        var wasClosed: Boolean = false
            private set

        override fun read(): Int {
            readCallCount += 1
            return super.read()
        }

        override fun read(target: ByteArray, offset: Int, length: Int): Int {
            readCallCount += 1
            return super.read(target, offset, length)
        }

        override fun close() {
            wasClosed = true
            super.close()
        }
    }

    private class OneByteAtATimeInputStream(bytes: ByteArray) : ByteArrayInputStream(bytes) {

        override fun read(target: ByteArray, offset: Int, length: Int): Int {
            return super.read(
                target,
                offset,
                minOf(length, SINGLE_BYTE),
            )
        }
    }

    private object FailingInputStream : InputStream() {

        override fun read(): Int {
            throw IOException("stream disconnected")
        }
    }
}

private const val SINGLE_BYTE = 1
