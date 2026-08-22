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
            ExpressionFormatter()

        return PrintScriptFormatter(
            statementFormatterDispatcher =
                createV1StatementFormatterDispatcher(
                    configuration = configuration,
                    expressionFormatter = expressionFormatter,
                ),
            separationPolicy =
                PrintScriptV1StatementSeparationPolicy(
                    lineBreaksBeforePrintln =
                        configuration.lineBreaksBeforePrintln,
                ),
        )
    }

    private fun createV1StatementFormatterDispatcher(
        configuration: FormatterConfiguration,
        expressionFormatter: ExpressionFormatter,
    ): StatementFormatterDispatcher {
        return StatementFormatterDispatcher(
            formatters =
                listOf(
                    DeclarationFormatter(
                        expressionFormatter = expressionFormatter,
                        spaceBeforeColon =
                            configuration.spaceBeforeColon,
                        spaceAfterColon =
                            configuration.spaceAfterColon,
                    ),
                    AssignmentFormatter(
                        expressionFormatter = expressionFormatter,
                    ),
                    PrintlnFormatter(
                        expressionFormatter = expressionFormatter,
                    ),
                ),
        )
    }
}