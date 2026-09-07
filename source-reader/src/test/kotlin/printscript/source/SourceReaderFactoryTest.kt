package printscript.source

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SourceReaderFactoryTest {

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

    private tailrec fun readAll(reader: SourceReader, accumulated: String = ""): String {
        return when (val result = reader.readChunk()) {
            SourceChunkReadResult.EndOfInput -> accumulated

            is SourceChunkReadResult.Failure -> fail("Unexpected source read failure: ${result.error}")

            is SourceChunkReadResult.Success ->
                readAll(result.remainingReader, accumulated + result.chunk.content)
        }
    }
}
