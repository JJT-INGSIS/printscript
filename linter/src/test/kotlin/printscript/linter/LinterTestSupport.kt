package printscript.linter

import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan
import printscript.statement.ParseError
import printscript.statement.Statement
import printscript.statement.StatementReadResult
import printscript.statement.StatementSource
import printscript.token.Token
import printscript.token.TokenType

private const val CONSUMED_STATEMENT_COUNT = 1
private const val SINGLE_DIAGNOSTIC = 1
private const val FIRST_LINE = 1
private const val FIRST_COLUMN = 1
private const val FIRST_OFFSET = 0L

internal val anySpan: SourceSpan = SourceSpan(
    start = SourcePosition(FIRST_LINE, FIRST_COLUMN, FIRST_OFFSET),
    end = SourcePosition(FIRST_LINE, FIRST_COLUMN, FIRST_OFFSET),
)

internal data class TestStatement(
    val name: String,
    override val span: SourceSpan = anySpan,
) : Statement

internal data class TestDiagnostic(
    val label: String,
    override val span: SourceSpan = anySpan,
) : Diagnostic

internal data object TestTokenType : TokenType

internal fun unexpectedTokenError(): ParseError {
    return ParseError.UnexpectedToken(
        expected = setOf(TestTokenType),
        actual = Token(
            type = TestTokenType,
            lexeme = "",
            span = anySpan,
        ),
    )
}

internal class NameReportingRule(
    private val label: String,
    private val reportedNames: Set<String>,
    private val reportCount: Int = SINGLE_DIAGNOSTIC,
) : StatelessLintRule() {

    protected override fun diagnosticsIn(statement: Statement): List<Diagnostic> {
        return statement
            .takeIf { candidate -> nameOf(candidate) in reportedNames }
            ?.let { reported -> diagnosticsFor(reported) }
            ?: emptyList()
    }

    private fun diagnosticsFor(statement: Statement): List<Diagnostic> {
        return List(reportCount) {
            TestDiagnostic(
                label = label,
                span = statement.span,
            )
        }
    }
}

internal class RepeatedNameRule(
    private val seenNames: Set<String>,
) : LintRule {

    constructor() : this(seenNames = emptySet())

    override fun inspect(statement: Statement): RuleInspection {
        val name = nameOf(statement)

        return RuleInspection(
            diagnostics = repetitionsOf(name, statement),
            resultingRule = RepeatedNameRule(seenNames = seenNames + name),
        )
    }

    private fun repetitionsOf(name: String, statement: Statement): List<Diagnostic> {
        return name
            .takeIf { candidate -> candidate in seenNames }
            ?.let { repeated -> listOf(TestDiagnostic(label = repeated, span = statement.span)) }
            ?: emptyList()
    }
}

private fun nameOf(statement: Statement): String {
    return (statement as TestStatement).name
}

internal class ListStatementSource(
    private val statements: List<Statement>,
) : StatementSource {

    override fun nextStatement(): StatementReadResult {
        val statement = statements.firstOrNull()
            ?: return StatementReadResult.EndOfInput

        return StatementReadResult.Success(
            statement = statement,
            remainingSource = ListStatementSource(
                statements = statements.drop(CONSUMED_STATEMENT_COUNT),
            ),
        )
    }
}

internal class FailingStatementSource(
    private val error: ParseError,
) : StatementSource {

    override fun nextStatement(): StatementReadResult {
        return StatementReadResult.Failure(error)
    }
}

internal class CountingStatementSource private constructor(
    private val source: StatementSource,
    private val readCounter: ReadCounter,
) : StatementSource {

    constructor(
        source: StatementSource,
    ) : this(
        source = source,
        readCounter = ReadCounter(),
    )

    val readCount: Int
        get() = readCounter.value

    override fun nextStatement(): StatementReadResult {
        readCounter.recordRead()

        return when (val readResult = source.nextStatement()) {
            is StatementReadResult.Success -> readResult.copy(
                remainingSource = CountingStatementSource(
                    source = readResult.remainingSource,
                    readCounter = readCounter,
                ),
            )

            is StatementReadResult.Failure -> readResult

            StatementReadResult.EndOfInput -> readResult
        }
    }
}

private class ReadCounter {

    var value: Int = 0
        private set

    fun recordRead() {
        value += CONSUMED_STATEMENT_COUNT
    }
}

internal fun statementsNamed(vararg names: String): StatementSource {
    return ListStatementSource(
        statements = names.map { name -> TestStatement(name) },
    )
}

internal fun DiagnosticSource.readAll(): List<Diagnostic> {
    return generateSequence(nextDiagnostic()) { previous ->
        continuationOf(previous)
    }
        .filterIsInstance<DiagnosticReadResult.Success>()
        .map { success -> success.diagnostic }
        .toList()
}

private fun continuationOf(result: DiagnosticReadResult): DiagnosticReadResult? {
    return when (result) {
        is DiagnosticReadResult.Success -> result.remainingSource.nextDiagnostic()

        is DiagnosticReadResult.Failure -> null

        DiagnosticReadResult.EndOfInput -> null
    }
}

internal fun List<Diagnostic>.labels(): List<String> {
    return map { diagnostic -> (diagnostic as TestDiagnostic).label }
}
