package printscript.formatter

import printscript.formatter.internal.PrintScriptFormatter
import printscript.formatter.internal.expression.ExpressionFormatter
import printscript.formatter.internal.separation.PrintScriptV1StatementSeparationPolicy
import printscript.formatter.internal.statement.AssignmentFormatter
import printscript.formatter.internal.statement.DeclarationFormatter
import printscript.formatter.internal.statement.PrintlnFormatter
import printscript.formatter.internal.statement.StatementFormatterDispatcher

public object PrintScriptFormatterFactory {

    public fun createV1(
        configuration: FormatterConfiguration,
    ): Formatter {
        val expressionFormatter =
            ExpressionFormatter(
                insertSpaceAroundBinaryOperators =
                    configuration.insertSpaceAroundBinaryOperators,
            )

        return PrintScriptFormatter(
            statementFormatterDispatcher =
                createV1StatementFormatterDispatcher(
                    configuration = configuration,
                    expressionFormatter = expressionFormatter,
                ),
            statementSeparationPolicy =
                PrintScriptV1StatementSeparationPolicy(
                    defaultLineBreakCountBetweenStatements =
                        configuration.lineBreakCountBetweenStatements,
                    lineBreakCountBeforeOutputStatements =
                        configuration.lineBreakCountBeforeOutputStatements,
                ),
        )
    }

    private fun createV1StatementFormatterDispatcher(
        configuration: FormatterConfiguration,
        expressionFormatter: ExpressionFormatter,
    ): StatementFormatterDispatcher {
        return StatementFormatterDispatcher(
            statementFormatters =
                listOf(
                    DeclarationFormatter(
                        expressionFormatter = expressionFormatter,
                        insertSpaceBeforeColon =
                            configuration.insertSpaceBeforeColon,
                        insertSpaceAfterColon =
                            configuration.insertSpaceAfterColon,
                        insertSpaceAroundEqualsOperator =
                            configuration.insertSpaceAroundEqualsOperator,
                    ),
                    AssignmentFormatter(
                        expressionFormatter = expressionFormatter,
                        insertSpaceAroundEqualsOperator =
                            configuration.insertSpaceAroundEqualsOperator,
                    ),
                    PrintlnFormatter(
                        expressionFormatter = expressionFormatter,
                    ),
                ),
        )
    }
}
