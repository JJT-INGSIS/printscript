package printscript.v1.formatter

import printscript.formatter.FormattedChunkReadResult
import printscript.formatter.FormattedSource
import printscript.formatter.TokenGap
import printscript.formatter.WhitespaceFormattingResult
import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan
import printscript.source.SourceReaderFactory
import printscript.token.Token
import printscript.v1.formatter.configuration.PrintScriptV11FormatterConfiguration
import printscript.v1.formatter.configuration.PrintScriptV11FormatterConfigurationResult
import printscript.v1.formatter.configuration.PrintScriptV1FormatterConfiguration
import printscript.v1.formatter.configuration.PrintScriptV1FormatterConfigurationResult
import printscript.v1.formatter.internal.whitespace.indentedWhitespace
import printscript.v1.formatter.internal.whitespace.repeatedWhitespace
import printscript.v1.lexer.PrintScriptV11LexerFactory
import printscript.v1.lexer.PrintScriptV1LexerFactory
import printscript.v1.token.PrintScriptV1TokenType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class FormattingQuantityTest {

    @Test
    fun `the mandatory line break is included in the final size validation`() {
        val formatter = PrintScriptV1FormatterFactory.create(
            PrintScriptV1FormatterConfiguration(blankLinesAfterPrintln = Int.MAX_VALUE),
        )
        val tokens = PrintScriptV1LexerFactory.create().tokenize(
            SourceReaderFactory.fromString("println(1);let value:number=1;"),
        )

        val failure = firstFormattingFailure(formatter.format(tokens))

        assertIs<WhitespaceSizeLimitExceeded>(failure.error)
    }

    @Test
    fun `programmatic configurations reject negative quantities`() {
        assertFailsWith<IllegalArgumentException> {
            PrintScriptV1FormatterConfiguration(blankLinesAfterPrintln = -1)
        }
        assertFailsWith<IllegalArgumentException> {
            PrintScriptV11FormatterConfiguration(indentationInsideIf = -1)
        }
    }

    @Test
    fun `JSON accepts every nonnegative quantity representable as an Int`() {
        val v1 = assertIs<PrintScriptV1FormatterConfigurationResult.Success>(
            PrintScriptV1FormatterConfiguration.fromJson(
                """{"line-breaks-after-println": ${Int.MAX_VALUE}}""",
            ),
        )
        assertEquals(Int.MAX_VALUE, v1.configuration.blankLinesAfterPrintln)

        val v11 = assertIs<PrintScriptV11FormatterConfigurationResult.Success>(
            PrintScriptV11FormatterConfiguration.fromJson(
                """{"indent-inside-if": ${Int.MAX_VALUE}}""",
            ),
        )
        assertEquals(Int.MAX_VALUE, v11.configuration.indentationInsideIf)
    }

    @Test
    fun `JSON quantities above the signed range are controlled configuration failures`() {
        val value = Int.MAX_VALUE.toLong() + 1L
        assertIs<PrintScriptV1FormatterConfigurationResult.Failure>(
            PrintScriptV1FormatterConfiguration.fromJson(
                """{"line-breaks-after-println": $value}""",
            ),
        )
        assertIs<PrintScriptV11FormatterConfigurationResult.Failure>(
            PrintScriptV11FormatterConfiguration.fromJson(
                """{"indent-inside-if": $value}""",
            ),
        )
    }

    @Test
    fun `checks multiplication and the existing prefix before generating indentation`() {
        val failures = listOf(
            indentedWhitespace(gap(), "\n", Int.MAX_VALUE, 2),
            indentedWhitespace(gap(), "\n", Int.MAX_VALUE, 1),
            indentedWhitespace(gap(), "", Int.MAX_VALUE, Int.MAX_VALUE),
            repeatedWhitespace(gap(), "\n", Int.MAX_VALUE.toLong() + 1L),
        )

        for (result in failures) {
            val failure = assertIs<WhitespaceFormattingResult.Failure>(result)
            assertIs<WhitespaceSizeLimitExceeded>(failure.error)
            assertEquals(gap().nextToken?.span, failure.error.span)
        }
    }

    @Test
    fun `zero indentation remains valid regardless of depth`() {
        assertEquals(
            WhitespaceFormattingResult.Success("\n"),
            indentedWhitespace(gap(), "\n", 0, Int.MAX_VALUE),
        )
        assertEquals(
            WhitespaceFormattingResult.Success("\n    "),
            indentedWhitespace(gap(), "\n", 2, 2),
        )
    }

    @Test
    fun `reports dynamic indentation overflow through the lazy formatting source`() {
        val formatter = PrintScriptV11FormatterFactory.create(
            PrintScriptV11FormatterConfiguration(indentationInsideIf = Int.MAX_VALUE),
        )
        val tokens = PrintScriptV11LexerFactory.create().tokenize(
            SourceReaderFactory.fromString("if(a){if(b){\nprintln(1);}}"),
        )

        val failure = firstFormattingFailure(formatter.format(tokens))

        assertIs<WhitespaceSizeLimitExceeded>(failure.error)
        assertEquals(2, failure.error.span.start.line)
    }

    private tailrec fun firstFormattingFailure(source: FormattedSource): FormattedChunkReadResult.Failure {
        return when (val result = source.nextFormattedChunk()) {
            is FormattedChunkReadResult.Success -> firstFormattingFailure(result.remainingSource)
            is FormattedChunkReadResult.Failure -> result
            FormattedChunkReadResult.EndOfInput -> error("Expected a formatting failure")
        }
    }

    private fun gap(): TokenGap {
        val position = SourcePosition.initial()
        val token = Token(
            type = PrintScriptV1TokenType.PRINTLN,
            lexeme = "println",
            span = SourceSpan(position, position.nextColumn()),
        )
        return TokenGap(previousToken = null, originalWhitespace = "", nextToken = token)
    }
}
