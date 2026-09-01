package printscript.source

import org.junit.jupiter.api.Assumptions.assumeTrue
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.fail

class SourceReaderFactoryTest {

    private fun scriptFile(content: String): Path {
        val file = Files.createTempFile("printscript", ".ps")
        file.toFile().deleteOnExit()
        Files.writeString(file, content)

        return file
    }

    /**
     * Vacía el lector encadenando `remainingReader`, que es la única forma
     * de recorrerlo: cada lectura devuelve el resto en vez de avanzar.
     */
    private tailrec fun readAll(reader: SourceReader, accumulated: String = ""): String {
        return when (val result = reader.readChunk()) {
            SourceChunkReadResult.EndOfInput -> accumulated

            is SourceChunkReadResult.Failure -> fail("Unexpected source read failure: ${result.error}")

            is SourceChunkReadResult.Success ->
                readAll(result.remainingReader, accumulated + result.chunk.content)
        }
    }

    private fun readerOf(creation: SourceReaderCreationResult): SourceReader {
        return assertIs<SourceReaderCreationResult.Success>(creation).reader
    }

    private fun errorOf(creation: SourceReaderCreationResult): SourceReaderCreationError {
        return assertIs<SourceReaderCreationResult.Failure>(creation).error
    }

    @Test
    fun `fromString delivers the whole content`() {
        val reader = SourceReaderFactory.fromString("let a: number = 5;")

        assertEquals(
            expected = "let a: number = 5;",
            actual = readAll(reader),
        )
    }

    @Test
    fun `fromString delivers nothing for empty code`() {
        assertEquals(
            expected = "",
            actual = readAll(SourceReaderFactory.fromString("")),
        )
    }

    @Test
    fun `fromPath delivers the content of the file`() {
        val file = scriptFile("println(1 + 2);")

        assertEquals(
            expected = "println(1 + 2);",
            actual = readAll(readerOf(SourceReaderFactory.fromPath(file))),
        )
    }

    @Test
    fun `fromPath delivers content longer than a single chunk`() {
        val longContent = "let a: number = 5;\n".repeat(1_000)
        val file = scriptFile(longContent)

        assertEquals(
            expected = longContent,
            actual = readAll(readerOf(SourceReaderFactory.fromPath(file))),
        )
    }

    @Test
    fun `fromPath preserves UTF-8 characters split across small byte buffers`() {
        val sourceCode = "Aá€😀B\r\nC"
        val file = scriptFile(sourceCode)

        assertEquals(
            expected = sourceCode,
            actual = readAll(
                readerOf(
                    SourceReaderFactory.fromPath(
                        path = file,
                        bufferSizeInBytes = 1,
                    ),
                ),
            ),
        )
    }

    @Test
    fun `fromPath interprets the configured buffer size as bytes`() {
        val file = scriptFile("áB")
        val reader = readerOf(
            SourceReaderFactory.fromPath(
                path = file,
                bufferSizeInBytes = 2,
            ),
        )

        val firstRead = assertIs<SourceChunkReadResult.Success>(reader.readChunk())
        val secondRead = assertIs<SourceChunkReadResult.Success>(firstRead.remainingReader.readChunk())

        assertEquals(expected = "á", actual = firstRead.chunk.content)
        assertEquals(expected = "B", actual = secondRead.chunk.content)
    }

    @Test
    fun `fromPath creates a lazy reader without consuming the file`() {
        val file = scriptFile("println(1);")
        val reader = readerOf(SourceReaderFactory.fromPath(file))

        Files.delete(file)

        val failure = assertIs<SourceChunkReadResult.Failure>(reader.readChunk())
        assertEquals(
            expected = SourceAccessError.NotFound(file),
            actual = failure.error,
        )
    }

    @Test
    fun `fromPath closes the file after each chunk read`() {
        val file = scriptFile("abcdef")
        val reader = readerOf(
            SourceReaderFactory.fromPath(
                path = file,
                bufferSizeInBytes = 3,
            ),
        )
        val firstRead = assertIs<SourceChunkReadResult.Success>(reader.readChunk())

        Files.delete(file)

        val failure = assertIs<SourceChunkReadResult.Failure>(firstRead.remainingReader.readChunk())
        assertEquals(
            expected = SourceAccessError.NotFound(file),
            actual = failure.error,
        )
    }

    @Test
    fun `fromPath reports invalid UTF-8 during lazy reading`() {
        val file = scriptFile("")
        Files.write(file, byteArrayOf(0xC3.toByte()))
        val reader = readerOf(SourceReaderFactory.fromPath(file))

        val failure = assertIs<SourceChunkReadResult.Failure>(reader.readChunk())

        assertEquals(
            expected = SourceReadError.InvalidEncoding(
                path = file,
                byteOffset = 0L,
            ),
            actual = failure.error,
        )
    }

    @Test
    fun `fromPath returns a valid prefix before an invalid UTF-8 byte`() {
        val file = scriptFile("")
        Files.write(file, byteArrayOf('a'.code.toByte(), 0xC3.toByte()))
        val reader = readerOf(SourceReaderFactory.fromPath(file))

        val prefix = assertIs<SourceChunkReadResult.Success>(reader.readChunk())
        val failure = assertIs<SourceChunkReadResult.Failure>(prefix.remainingReader.readChunk())

        assertEquals(expected = "a", actual = prefix.chunk.content)
        assertEquals(
            expected = SourceReadError.InvalidEncoding(
                path = file,
                byteOffset = 1L,
            ),
            actual = failure.error,
        )
    }

    @Test
    fun `fromPath reader states are immutable`() {
        val file = scriptFile("abcdef")
        val reader = readerOf(
            SourceReaderFactory.fromPath(
                path = file,
                bufferSizeInBytes = 3,
            ),
        )

        val firstRead = assertIs<SourceChunkReadResult.Success>(reader.readChunk())
        val repeatedRead = assertIs<SourceChunkReadResult.Success>(reader.readChunk())
        val nextRead = assertIs<SourceChunkReadResult.Success>(firstRead.remainingReader.readChunk())

        assertEquals(expected = firstRead, actual = repeatedRead)
        assertEquals(expected = "abc", actual = firstRead.chunk.content)
        assertEquals(expected = "def", actual = nextRead.chunk.content)
        assertNotEquals(illegal = firstRead, actual = nextRead)
    }

    @Test
    fun `fromPath rejects a zero byte buffer`() {
        val file = scriptFile("let a: number = 5;")

        assertEquals(
            expected = SourceReaderCreationError.InvalidBufferSize(0),
            actual = errorOf(
                SourceReaderFactory.fromPath(
                    path = file,
                    bufferSizeInBytes = 0,
                ),
            ),
        )
    }

    @Test
    fun `fromPath rejects a negative byte buffer`() {
        val file = scriptFile("let a: number = 5;")

        assertEquals(
            expected = SourceReaderCreationError.InvalidBufferSize(-1),
            actual = errorOf(
                SourceReaderFactory.fromPath(
                    path = file,
                    bufferSizeInBytes = -1,
                ),
            ),
        )
    }

    @Test
    fun `fromPath reports a missing file`() {
        val missing = Path.of("no", "existe", "archivo.ps")

        assertEquals(
            expected = SourceAccessError.NotFound(missing),
            actual = errorOf(SourceReaderFactory.fromPath(missing)),
        )
    }

    @Test
    fun `fromPath reports a directory`() {
        val directory = Files.createTempDirectory("printscript")
        directory.toFile().deleteOnExit()

        assertEquals(
            expected = SourceAccessError.NotAFile(directory),
            actual = errorOf(SourceReaderFactory.fromPath(directory)),
        )
    }

    @Test
    fun `fromPath reports an unreadable file when the platform can deny read access`() {
        val file = scriptFile("let a: number = 5;")
        val permissionChangeSucceeded = file.toFile().setReadable(false)

        try {
            assumeTrue(
                permissionChangeSucceeded && !Files.isReadable(file),
                "The platform did not make the temporary file unreadable",
            )

            assertEquals(
                expected = SourceAccessError.NotReadable(file),
                actual = errorOf(SourceReaderFactory.fromPath(file)),
            )
        } finally {
            file.toFile().setReadable(true)
        }
    }

    @Test
    fun `every access error carries the path that failed`() {
        val missing = Path.of("no", "existe", "archivo.ps")

        val errors = listOf(
            SourceAccessError.NotFound(missing),
            SourceAccessError.NotAFile(missing),
            SourceAccessError.NotReadable(missing),
            SourceAccessError.ReadFailed(missing, "disco desconectado"),
        )

        for (error in errors) {
            assertEquals(
                expected = missing,
                actual = error.path,
            )
        }
    }
}
