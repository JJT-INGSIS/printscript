package printscript.v1.formatter

import printscript.formatter.Formatter
import printscript.formatter.FormatterFactory
import printscript.formatter.StatementFormatter
import printscript.formatter.StatementSeparationPolicy
import printscript.v1.formatter.internal.expression.ExpressionFormatter
import printscript.v1.formatter.internal.separation.PrintScriptV1StatementSeparationPolicy
import printscript.v1.formatter.internal.statement.AssignmentFormatter
import printscript.v1.formatter.internal.statement.DeclarationFormatter
import printscript.v1.formatter.internal.statement.PrintlnFormatter

private const val SINGLE_LINE_BREAK: UInt = 1u

public object PrintScriptV1FormatterFactory {

    public fun defaultConfiguration(): PrintScriptV1FormatterConfiguration {
        return PrintScriptV1FormatterConfiguration(
            insertSpaceBeforeColon = false,
            insertSpaceAfterColon = true,
            insertSpaceAroundEqualsOperator = true,
            insertSpaceAroundBinaryOperators = true,
            lineBreakCountBetweenStatements = SINGLE_LINE_BREAK,
            lineBreakCountBeforeOutputStatements = SINGLE_LINE_BREAK,
        )
    }

    /**
     * Creates the V1 formatter. Additional statement formatters are evaluated
     * before the formatters included by V1, allowing callers to extend or
     * override them. The separation policy may also be replaced independently
     * from statement formatting.
     */
    public fun create(
        configuration: PrintScriptV1FormatterConfiguration = defaultConfiguration(),
        additionalStatementFormatters: List<StatementFormatter> = emptyList(),
        statementSeparationPolicy: StatementSeparationPolicy =
            defaultStatementSeparationPolicy(configuration),
    ): Formatter {
        val expressionFormatter = ExpressionFormatter(
            insertSpaceAroundBinaryOperators = configuration.insertSpaceAroundBinaryOperators,
        )

        return FormatterFactory.create(
            statementFormatters =
            additionalStatementFormatters +
                printScriptV1StatementFormatters(
                    configuration = configuration,
                    expressionFormatter = expressionFormatter,
                ),
            statementSeparationPolicy = statementSeparationPolicy,
        )
    }

    private fun defaultStatementSeparationPolicy(
        configuration: PrintScriptV1FormatterConfiguration,
    ): StatementSeparationPolicy {
        return PrintScriptV1StatementSeparationPolicy(
            defaultLineBreakCountBetweenStatements = configuration.lineBreakCountBetweenStatements,
            lineBreakCountBeforeOutputStatements = configuration.lineBreakCountBeforeOutputStatements,
        )
    }

    private fun printScriptV1StatementFormatters(
        configuration: PrintScriptV1FormatterConfiguration,
        expressionFormatter: ExpressionFormatter,
    ): List<StatementFormatter> {
        return listOf(
            DeclarationFormatter(
                expressionFormatter = expressionFormatter,
                insertSpaceBeforeColon = configuration.insertSpaceBeforeColon,
                insertSpaceAfterColon = configuration.insertSpaceAfterColon,
                insertSpaceAroundEqualsOperator = configuration.insertSpaceAroundEqualsOperator,
            ),
            AssignmentFormatter(
                expressionFormatter = expressionFormatter,
                insertSpaceAroundEqualsOperator = configuration.insertSpaceAroundEqualsOperator,
            ),
            PrintlnFormatter(
                expressionFormatter = expressionFormatter,
            ),
        )
    }
}
