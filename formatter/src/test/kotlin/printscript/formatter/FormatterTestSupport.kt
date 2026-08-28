package printscript.formatter

import printscript.ast.statement.Statement
import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan
import printscript.statement.ParseError
import printscript.statement.StatementReadResult
import printscript.statement.StatementSource

internal val testSpan: SourceSpan = SourceSpan(
    start = SourcePosition.initial(),
    end = SourcePosition.initial().nextColumn(),
)

internal data class TestStatement(
    val value: String,
    override val span: SourceSpan = testSpan,
) : Statement

internal data class TestParseError(
    override val span: SourceSpan = testSpan,
) : ParseError

internal data class TestFormattingError(
    override val span: SourceSpan = testSpan,
) : FormattingError

internal data class ListStatementSource(
    private val statements: List<Statement>,
) : StatementSource {

    override fun nextStatement(): StatementReadResult {
        if (statements.isEmpty()) {
            return StatementReadResult.EndOfInput
        }

        return StatementReadResult.Success(
            statement = statements.first(),
            remainingSource = copy(
                statements = statements.drop(1),
            ),
        )
    }
}

internal data class FailingStatementSource(
    private val error: ParseError,
) : StatementSource {

    override fun nextStatement(): StatementReadResult {
        return StatementReadResult.Failure(error)
    }
}

internal class CountingStatementSource private constructor(
    private val delegate: StatementSource,
    private val counter: StatementReadCounter,
) : StatementSource {

    internal constructor(delegate: StatementSource) : this(
        delegate = delegate,
        counter = StatementReadCounter(),
    )

    internal val readCount: Int
        get() = counter.value

    override fun nextStatement(): StatementReadResult {
        counter.increment()

        return when (val result = delegate.nextStatement()) {
            is StatementReadResult.Success -> result.copy(
                remainingSource = CountingStatementSource(result.remainingSource, counter),
            )

            is StatementReadResult.Failure -> result
            StatementReadResult.EndOfInput -> result
        }
    }
}

private class StatementReadCounter {

    var value: Int = 0
        private set

    fun increment() {
        value += 1
    }
}
