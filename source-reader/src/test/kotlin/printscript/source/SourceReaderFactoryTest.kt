package printscript.source

import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

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

            is SourceChunkReadResult.Success ->
                readAll(result.remainingReader, accumulated + result.chunk.content)
        }
    }

    private fun readerOf(creation: SourceReaderCreationResult): SourceReader {
        return assertIs<SourceReaderCreationResult.Success>(creation).reader
    }

    private fun errorOf(creation: SourceReaderCreationResult): SourceAccessError {
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
    fun `fromPath reports a file that cannot be read`() {
        val file = scriptFile("let a: number = 5;")
        file.toFile().setReadable(false)

        assertEquals(
            expected = SourceAccessError.NotReadable(file),
            actual = errorOf(SourceReaderFactory.fromPath(file)),
        )

        file.toFile().setReadable(true)
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
