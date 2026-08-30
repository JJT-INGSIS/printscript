package printscript.parser

import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan
import printscript.statement.Statement
import printscript.token.Token
import printscript.token.TokenReadResult
import printscript.token.TokenSource
import printscript.token.TokenType

internal enum class TestTokenType : TokenType {
    WORD,
    NUMBER,
    PLUS,
    STAR,
    MINUS,
    OPEN,
    CLOSE,
    TERMINATOR,
    EOF,
}

internal data class TestStatement(
    val value: String,
    override val span: SourceSpan,
) : Statement

internal data class TestBlockStatement(
    val statements: List<Statement>,
    override val span: SourceSpan,
) : Statement

internal fun tokenSourceOf(vararg tokens: Pair<TestTokenType, String>): TokenSource {
    return ListTokenSource(
        tokens = tokens.mapIndexed { index, (type, lexeme) -> token(type, lexeme, index) },
    )
}

internal fun token(type: TokenType, lexeme: String, index: Int = 0): Token {
    val start = SourcePosition(
        line = 1,
        column = index + 1,
        offset = index.toLong(),
    )

    return Token(
        type = type,
        lexeme = lexeme,
        span = SourceSpan(
            start = start,
            end = start.copy(
                column = start.column + lexeme.length,
                offset = start.offset + lexeme.length,
            ),
        ),
    )
}

internal data class ListTokenSource(
    private val tokens: List<Token>,
) : TokenSource {

    override fun nextToken(): TokenReadResult {
        val token = tokens.first()

        return TokenReadResult.Success(
            token = token,
            remainingSource = copy(tokens = tokens.drop(1)),
        )
    }
}

internal class CountingTokenSource private constructor(
    private val delegate: TokenSource,
    private val counter: ReadCounter,
) : TokenSource {

    constructor(delegate: TokenSource) : this(
        delegate = delegate,
        counter = ReadCounter(),
    )

    val readCount: Int
        get() = counter.value

    override fun nextToken(): TokenReadResult {
        counter.increment()

        val result = delegate.nextToken()

        return when (result) {
            is TokenReadResult.Success -> result.copy(
                remainingSource = CountingTokenSource(result.remainingSource, counter),
            )

            is TokenReadResult.Failure -> result.copy(
                remainingSource = CountingTokenSource(result.remainingSource, counter),
            )
        }
    }
}

private class ReadCounter {

    var value: Int = 0
        private set

    fun increment() {
        value += 1
    }
}
