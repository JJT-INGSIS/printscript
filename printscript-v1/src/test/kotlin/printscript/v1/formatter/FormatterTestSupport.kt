package printscript.v1.formatter

import printscript.ast.Identifier
import printscript.ast.expression.NumberLiteralExpression
import printscript.ast.statement.Statement
import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan
import printscript.statement.StatementReadResult
import printscript.statement.StatementSource
import java.math.BigDecimal

internal val testSpan: SourceSpan = SourceSpan(
    start = SourcePosition.initial(),
    end = SourcePosition.initial().nextColumn(),
)

internal fun identifier(value: String): Identifier {
    return Identifier(
        value = value,
        span = testSpan,
    )
}

internal fun numberLiteral(value: String): NumberLiteralExpression {
    return NumberLiteralExpression(
        value = BigDecimal(value),
        span = testSpan,
    )
}

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
