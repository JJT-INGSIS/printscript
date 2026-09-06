package printscript.v1.formatter

import printscript.formatter.FormattedChunkReadResult
import printscript.formatter.TokenGap
import printscript.formatter.WhitespaceFormattingResult
import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan
import printscript.source.SourceReaderFactory
import printscript.token.Token
import printscript.v1.formatter.internal.indentedWhitespace
import printscript.v1.formatter.internal.repeatedWhitespace
import printscript.v1.lexer.PrintScriptV11FormattingLexerFactory
import printscript.v1.token.PrintScriptV1TokenType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class FormattingQuantityTest {

    @Test
    fun `JSON rejects a blank line count whose mandatory line break would overflow`() {
        val json = """{"line-breaks-after-println": ${Int.MAX_VALUE}}"""
        val v1Failure = assertIs<PrintScriptV1FormatterConfigurationResult.Failure>(
            PrintScriptV1FormatterFactory.configurationFrom(json),
        )
        assertIs<PrintScriptV1FormatterConfigurationError.ExcessiveLineBreakCount>(v1Failure.error)
        val v11Failure = assertIs<PrintScriptV11FormatterConfigurationResult.Failure>(
            PrintScriptV11FormatterFactory.configurationFrom(json),
        )
        val inherited = assertIs<PrintScriptV11FormatterConfigurationError.V1ConfigurationFailure>(v11Failure.error)
        assertEquals(v1Failure.error, inherited.error)
    }

    @Test
    fun `programmatic configurations reject quantities outside the supported range`() {
        listOf(Int.MAX_VALUE.toUInt(), UInt.MAX_VALUE).forEach { value ->
            assertFailsWith<IllegalArgumentException> {
                PrintScriptV1FormatterConfiguration(lineBreaksAfterPrintln = value)
            }
        }
        assertFailsWith<IllegalArgumentException> {
            PrintScriptV11FormatterConfiguration(indentationInsideIf = UInt.MAX_VALUE)
        }
    }

    @Test
    fun `accepts representable configuration boundaries without allocating their output`() {
        val maximumBlankLines = Int.MAX_VALUE - 1
        val v1 = assertIs<PrintScriptV1FormatterConfigurationResult.Success>(
            PrintScriptV1FormatterFactory.configurationFrom("""{"line-breaks-after-println": $maximumBlankLines}"""),
        )
        assertEquals(maximumBlankLines.toUInt(), v1.configuration.lineBreaksAfterPrintln)
        val v11 = assertIs<PrintScriptV11FormatterConfigurationResult.Success>(
            PrintScriptV11FormatterFactory.configurationFrom("""{"indent-inside-if": ${Int.MAX_VALUE}}"""),
        )
        assertEquals(Int.MAX_VALUE.toUInt(), v11.configuration.indentationInsideIf)
    }

    @Test
    fun `JSON quantities above the signed range are controlled configuration failures`() {
        val value = UInt.MAX_VALUE
        assertIs<PrintScriptV1FormatterConfigurationResult.Failure>(
            PrintScriptV1FormatterFactory.configurationFrom("""{"line-breaks-after-println": $value}"""),
        )
        assertIs<PrintScriptV11FormatterConfigurationResult.Failure>(
            PrintScriptV11FormatterFactory.configurationFrom("""{"indent-inside-if": $value}"""),
        )
    }

    @Test
    fun `checks multiplication and the existing prefix before generating indentation`() {
        val failures = listOf(
            indentedWhitespace(gap(), "\n", Int.MAX_VALUE.toUInt(), 2uL),
            indentedWhitespace(gap(), "\n", Int.MAX_VALUE.toUInt(), 1uL),
            indentedWhitespace(gap(), "", UInt.MAX_VALUE, ULong.MAX_VALUE),
            repeatedWhitespace(gap(), "\n", Int.MAX_VALUE.toULong() + 1uL),
        )

        failures.forEach { result ->
            val failure = assertIs<WhitespaceFormattingResult.Failure>(result)
            assertIs<PrintScriptFormattingError.WhitespaceSizeOverflow>(failure.error)
            assertEquals(gap().nextToken?.span, failure.error.span)
        }
    }

    @Test
    fun `zero indentation remains valid regardless of depth`() {
        assertEquals(
            WhitespaceFormattingResult.Success("\n"),
            indentedWhitespace(gap(), "\n", 0u, ULong.MAX_VALUE),
        )
        assertEquals(
            WhitespaceFormattingResult.Success("\n    "),
            indentedWhitespace(gap(), "\n", 2u, 2uL),
        )
    }

    @Test
    fun `reports dynamic indentation overflow through the lazy formatting source`() {
        val formatter = PrintScriptV11FormatterFactory.create(
            PrintScriptV11FormatterConfiguration(indentationInsideIf = Int.MAX_VALUE.toUInt()),
        )
        val tokens = PrintScriptV11FormattingLexerFactory.create().tokenize(
            SourceReaderFactory.fromString("if(a){if(b){\nprintln(1);}}"),
        )
        var source = formatter.format(tokens)
        while (true) {
            when (val result = source.nextFormattedChunk()) {
                is FormattedChunkReadResult.Success -> source = result.remainingSource
                is FormattedChunkReadResult.Failure -> {
                    assertIs<PrintScriptFormattingError.WhitespaceSizeOverflow>(result.error)
                    assertEquals(2, result.error.span.start.line)
                    return
                }

                FormattedChunkReadResult.EndOfInput -> error("Expected an indentation failure")
            }
        }
    }

    private fun gap(): TokenGap {
        val position = SourcePosition.initial()
        val token = Token(PrintScriptV1TokenType.PRINTLN, "println", SourceSpan(position, position.nextColumn()))
        return TokenGap(previousToken = null, originalWhitespace = "", nextToken = token)
    }
}
