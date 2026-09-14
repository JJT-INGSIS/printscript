package printscript.cli

import printscript.cli.internal.report.SpanRenderer
import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan
import kotlin.test.Test
import kotlin.test.assertEquals

class SpanRendererTest {

    @Test
    fun `renders a span contained in a single line`() {
        val span = SourceSpan(
            start = SourcePosition(line = 3, column = 5, offset = 20),
            end = SourcePosition(line = 3, column = 9, offset = 24),
        )

        assertEquals(
            expected = "línea 3, columnas 5 a 9",
            actual = SpanRenderer.render(span),
        )
    }

    @Test
    fun `names both ends of a span that crosses lines`() {
        val span = SourceSpan(
            start = SourcePosition(line = 2, column = 7, offset = 30),
            end = SourcePosition(line = 4, column = 1, offset = 55),
        )

        assertEquals(
            expected = "línea 2, columna 7 a línea 4, columna 1",
            actual = SpanRenderer.render(span),
        )
    }
}
