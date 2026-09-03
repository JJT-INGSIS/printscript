package printscript.formatter

import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan
import printscript.token.Token
import printscript.token.TokenReadError
import printscript.token.TokenReadResult
import printscript.token.TokenSource
import printscript.token.TokenType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame

class FormatterFactoryTest {

    @Test
    fun `does not read tokens until formatted output is requested`() {
        val tokenSource = CountingTokenSource(
            ListTokenSource(
                tokens = listOf(token(TestTokenType.WORD, "first")),
            ),
        )
        val formattedSource = formatterWith().format(tokenSource)

        assertEquals(expected = 0, actual = tokenSource.readCount)

        formattedSource.nextFormattedChunk()

        assertEquals(expected = 1, actual = tokenSource.readCount)
    }

    @Test
    fun `preserves every original gap when no rule supports it`() {
        val formattedText = formatAll(
            formatter = formatterWith(),
            tokenSource = ListTokenSource(
                tokens = listOf(
                    token(TestTokenType.WHITESPACE, "  "),
                    token(TestTokenType.WORD, "first"),
                    token(TestTokenType.WHITESPACE, "\t"),
                    token(TestTokenType.WORD, "second"),
                    token(TestTokenType.WHITESPACE, "\r\n"),
                ),
            ),
        )

        assertEquals(expected = "  first\tsecond\r\n", actual = formattedText)
    }

    @Test
    fun `uses the first rule that supports a gap`() {
        val formattedText = formatAll(
            formatter = formatterWith(
                formattingRules = listOf(
                    ReplacingGapRule("<first>"),
                    ReplacingGapRule("<second>"),
                ),
            ),
            tokenSource = words("left", "right"),
        )

        assertEquals(expected = "left<first>right", actual = formattedText)
    }

    @Test
    fun `can insert whitespace into an empty gap`() {
        val formattedText = formatAll(
            formatter = formatterWith(
                formattingRules = listOf(ReplacingGapRule(" ")),
            ),
            tokenSource = words("left", "right"),
        )

        assertEquals(expected = "left right", actual = formattedText)
    }

    @Test
    fun `copies configured rules defensively`() {
        val rules = mutableListOf<TokenGapFormattingRule>(
            ReplacingGapRule(" "),
        )
        val formatter = formatterWith(rules)

        rules.clear()

        assertEquals(
            expected = "left right",
            actual = formatAll(formatter, words("left", "right")),
        )
    }

    @Test
    fun `uses the immutable rule state returned after consuming a token`() {
        val formattedText = formatAll(
            formatter = formatterWith(
                formattingRules = listOf(AfterMarkerRule()),
            ),
            tokenSource = ListTokenSource(
                tokens = listOf(
                    token(TestTokenType.MARKER, "marker"),
                    token(TestTokenType.WORD, "value"),
                ),
            ),
        )

        assertEquals(expected = "marker!value", actual = formattedText)
    }

    @Test
    fun `propagates token read failures`() {
        val expectedError = TestTokenReadError()
        val result = formatterWith()
            .format(FailingTokenSource(expectedError))
            .nextFormattedChunk()

        val formattingError = assertIs<FormattingError.TokenReadFailure>(
            assertIs<FormattedChunkReadResult.Failure>(result).error,
        )

        assertSame(expected = expectedError, actual = formattingError.tokenReadError)
    }

    @Test
    fun `reports end of input for an empty token source`() {
        val result = formatterWith()
            .format(ListTokenSource(tokens = emptyList()))
            .nextFormattedChunk()

        assertIs<FormattedChunkReadResult.EndOfInput>(result)
    }

    @Test
    fun `returns trailing whitespace before reporting end of input`() {
        val source = formatterWith().format(
            ListTokenSource(
                tokens = listOf(token(TestTokenType.WHITESPACE, "\n")),
            ),
        )

        val trailingWhitespace = assertIs<FormattedChunkReadResult.Success>(
            source.nextFormattedChunk(),
        )

        assertEquals(expected = "\n", actual = trailingWhitespace.formattedText)
        assertIs<FormattedChunkReadResult.EndOfInput>(
            trailingWhitespace.remainingSource.nextFormattedChunk(),
        )
    }

    private fun formatterWith(formattingRules: List<TokenGapFormattingRule> = emptyList()): Formatter {
        return FormatterFactory.create(
            formattingRules = formattingRules,
            whitespaceTokenType = TestTokenType.WHITESPACE,
            endOfInputTokenType = TestTokenType.EOF,
        )
    }

    private fun words(vararg lexemes: String): TokenSource {
        return ListTokenSource(
            tokens = lexemes.map { lexeme ->
                token(TestTokenType.WORD, lexeme)
            },
        )
    }

    private fun formatAll(formatter: Formatter, tokenSource: TokenSource): String {
        var source = formatter.format(tokenSource)
        val result = StringBuilder()

        while (true) {
            when (val readResult = source.nextFormattedChunk()) {
                is FormattedChunkReadResult.Success -> {
                    result.append(readResult.formattedText)
                    source = readResult.remainingSource
                }

                is FormattedChunkReadResult.Failure ->
                    error("Unexpected formatting failure: ${readResult.error}")

                FormattedChunkReadResult.EndOfInput -> return result.toString()
            }
        }
    }
}

private enum class TestTokenType : TokenType {
    WORD,
    MARKER,
    WHITESPACE,
    EOF,
}

private val testSpan = SourceSpan(
    start = SourcePosition.initial(),
    end = SourcePosition.initial().nextColumn(),
)

private fun token(type: TokenType, lexeme: String): Token {
    return Token(
        type = type,
        lexeme = lexeme,
        span = testSpan,
    )
}

private data class ListTokenSource(
    private val tokens: List<Token>,
) : TokenSource {

    override fun nextToken(): TokenReadResult {
        if (tokens.isEmpty()) {
            return TokenReadResult.Success(
                token = token(TestTokenType.EOF, ""),
                remainingSource = this,
            )
        }

        return TokenReadResult.Success(
            token = tokens.first(),
            remainingSource = copy(tokens = tokens.drop(1)),
        )
    }
}

private class CountingTokenSource private constructor(
    private val delegate: TokenSource,
    private val counter: TokenReadCounter,
) : TokenSource {

    constructor(delegate: TokenSource) : this(delegate, TokenReadCounter())

    val readCount: Int
        get() = counter.value

    override fun nextToken(): TokenReadResult {
        counter.increment()

        return when (val result = delegate.nextToken()) {
            is TokenReadResult.Failure -> result
            is TokenReadResult.Success ->
                result.copy(
                    remainingSource = CountingTokenSource(result.remainingSource, counter),
                )
        }
    }
}

private class TokenReadCounter {

    var value: Int = 0
        private set

    fun increment() {
        value += 1
    }
}

private data class FailingTokenSource(
    private val error: TokenReadError,
) : TokenSource {

    override fun nextToken(): TokenReadResult {
        return TokenReadResult.Failure(
            error = error,
            remainingSource = this,
        )
    }
}

private data class TestTokenReadError(
    override val span: SourceSpan = testSpan,
) : TokenReadError

private data class ReplacingGapRule(
    private val replacement: String,
) : TokenGapFormattingRule {

    override fun supports(gap: TokenGap): Boolean {
        return gap.previousToken != null && gap.nextToken != null
    }

    override fun formatWhitespace(gap: TokenGap): String {
        return replacement
    }
}

private data class AfterMarkerRule(
    private val markerWasConsumed: Boolean = false,
) : TokenGapFormattingRule {

    override fun supports(gap: TokenGap): Boolean {
        return markerWasConsumed && gap.nextToken != null
    }

    override fun formatWhitespace(gap: TokenGap): String {
        return "!"
    }

    override fun afterConsuming(token: Token): TokenGapFormattingRule {
        return copy(markerWasConsumed = token.type == TestTokenType.MARKER)
    }
}
