package printscript.v1.formatter

import printscript.formatter.Formatter
import printscript.formatter.FormatterFactory
import printscript.formatter.StatementFormatter
import printscript.formatter.StatementSeparationPolicy
import printscript.v1.formatter.internal.expression.ExpressionFormatter
import printscript.v1.formatter.internal.separation.PrintScriptV1StatementSeparationPolicy
import printscript.v1.formatter.internal.statement.AssignmentFormatter
import printscript.v1.formatter.internal.statement.PrintlnFormatter
import printscript.v1.formatter.internal.statement.VariableDeclarationFormatter

private const val SINGLE_LINE_BREAK_COUNT: UInt = 1u

public object PrintScriptV1FormatterFactory {

    public fun defaultConfiguration(): PrintScriptV1FormatterConfiguration {
        return PrintScriptV1FormatterConfiguration(
            insertSpaceBeforeColon = false,
            insertSpaceAfterColon = true,
            insertSpaceAroundEqualsOperator = true,
            insertSpaceAroundBinaryOperators = true,
            lineBreakCountBetweenStatements = SINGLE_LINE_BREAK_COUNT,
        )
    }

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
            lineBreakCountBetweenStatements = configuration.lineBreakCountBetweenStatements,
        )
    }

    private fun printScriptV1StatementFormatters(
        configuration: PrintScriptV1FormatterConfiguration,
        expressionFormatter: ExpressionFormatter,
    ): List<StatementFormatter> {
        return listOf(
            VariableDeclarationFormatter(
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
