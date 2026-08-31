package printscript.v1.formatter

import printscript.ast.DeclaredType
import printscript.ast.expression.BinaryExpression
import printscript.ast.expression.BinaryOperator
import printscript.ast.expression.Expression
import printscript.ast.expression.GroupingExpression
import printscript.ast.expression.IdentifierExpression
import printscript.ast.expression.StringLiteralExpression
import printscript.ast.expression.StringQuoteStyle
import printscript.ast.expression.UnaryExpression
import printscript.ast.expression.UnaryOperator
import printscript.ast.statement.AssignmentStatement
import printscript.ast.statement.PrintlnStatement
import printscript.ast.statement.VariableDeclarationStatement
import printscript.formatter.FormattedStatementReadResult
import printscript.formatter.Formatter
import printscript.formatter.StatementFormatter
import printscript.formatter.StatementFormattingResult
import printscript.formatter.StatementSeparationPolicy
import printscript.statement.Statement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class PrintScriptV1FormatterFactoryTest {

    @Test
    fun `formats every V1 statement using default configuration`() {
        val declaration = VariableDeclarationStatement(
            identifier = identifier("total"),
            declaredType = DeclaredType.NUMBER,
            initializer = BinaryExpression(
                left = numberLiteral("1"),
                operator = BinaryOperator.ADD,
                operatorSpan = testSpan,
                right = numberLiteral("2"),
            ),
            span = testSpan,
        )
        val assignment = AssignmentStatement(
            target = identifier("total"),
            expression = numberLiteral("3"),
            span = testSpan,
        )
        val output = PrintlnStatement(
            argument = StringLiteralExpression(
                value = "done",
                quoteStyle = StringQuoteStyle.SINGLE,
                span = testSpan,
            ),
            span = testSpan,
        )

        val formattedText = formatAll(
            formatter = PrintScriptV1FormatterFactory.create(),
            statements = listOf(declaration, assignment, output),
        )

        assertEquals(
            expected = "let total: number = 1 + 2;\ntotal = 3;\nprintln('done');",
            actual = formattedText,
        )
    }

    @Test
    fun `applies V1 spacing and separation configuration`() {
        val configuration = PrintScriptV1FormatterConfiguration(
            insertSpaceBeforeColon = true,
            insertSpaceAfterColon = false,
            insertSpaceAroundEqualsOperator = false,
            insertSpaceAroundBinaryOperators = false,
            lineBreakCountBetweenStatements = 2u,
        )
        val declaration = VariableDeclarationStatement(
            identifier = identifier("value"),
            declaredType = DeclaredType.NUMBER,
            initializer = binaryExpression(
                left = numberLiteral("1"),
                operator = BinaryOperator.ADD,
                right = numberLiteral("2"),
            ),
            span = testSpan,
        )
        val output = PrintlnStatement(
            argument = numberLiteral("1"),
            span = testSpan,
        )

        val formattedText = formatAll(
            formatter = PrintScriptV1FormatterFactory.create(
                configuration = configuration,
            ),
            statements = listOf(declaration, output),
        )

        assertEquals(
            expected = "let value :number=1+2;\n\nprintln(1);",
            actual = formattedText,
        )
    }

    @Test
    fun `formats every V1 expression variant`() {
        val expression = GroupingExpression(
            expression = binaryExpression(
                left = unaryExpression(
                    operator = UnaryOperator.PLUS,
                    operand = IdentifierExpression(identifier("value")),
                ),
                operator = BinaryOperator.SUBTRACT,
                right = binaryExpression(
                    left = numberLiteral("3"),
                    operator = BinaryOperator.MULTIPLY,
                    right = binaryExpression(
                        left = numberLiteral("4"),
                        operator = BinaryOperator.DIVIDE,
                        right = unaryExpression(
                            operator = UnaryOperator.MINUS,
                            operand = numberLiteral("5"),
                        ),
                    ),
                ),
            ),
            span = testSpan,
        )
        val doubleQuotedText = StringLiteralExpression(
            value = "done",
            quoteStyle = StringQuoteStyle.DOUBLE,
            span = testSpan,
        )

        val formattedText = formatAll(
            formatter = PrintScriptV1FormatterFactory.create(),
            statements = listOf(
                PrintlnStatement(
                    argument = expression,
                    span = testSpan,
                ),
                PrintlnStatement(
                    argument = doubleQuotedText,
                    span = testSpan,
                ),
            ),
        )

        assertEquals(
            expected = "println((+value - 3 * 4 / -5));\nprintln(\"done\");",
            actual = formattedText,
        )
    }

    @Test
    fun `formats declarations without initializer`() {
        val declaration = VariableDeclarationStatement(
            identifier = identifier("name"),
            declaredType = DeclaredType.STRING,
            initializer = null,
            span = testSpan,
        )

        val formattedText = formatAll(
            formatter = PrintScriptV1FormatterFactory.create(),
            statements = listOf(declaration),
        )

        assertEquals(expected = "let name: string;", actual = formattedText)
    }

    @Test
    fun `additional formatters have priority and are copied defensively`() {
        val additionalFormatters = mutableListOf<StatementFormatter>(
            OverridingVariableDeclarationFormatter,
        )
        val formatter = PrintScriptV1FormatterFactory.create(
            additionalStatementFormatters = additionalFormatters,
        )

        additionalFormatters.clear()

        val formattedText = formatAll(
            formatter = formatter,
            statements = listOf(
                VariableDeclarationStatement(
                    identifier = identifier("value"),
                    declaredType = DeclaredType.STRING,
                    initializer = null,
                    span = testSpan,
                ),
            ),
        )

        assertEquals(expected = "overridden", actual = formattedText)
    }

    @Test
    fun `allows replacing V1 statement separation policy`() {
        val formattedText = formatAll(
            formatter = PrintScriptV1FormatterFactory.create(
                statementSeparationPolicy = PipeSeparationPolicy,
            ),
            statements = listOf(
                PrintlnStatement(
                    argument = numberLiteral("1"),
                    span = testSpan,
                ),
                PrintlnStatement(
                    argument = numberLiteral("2"),
                    span = testSpan,
                ),
            ),
        )

        assertEquals(expected = "println(1);|println(2);", actual = formattedText)
    }

    private fun formatAll(formatter: Formatter, statements: List<Statement>): String {
        var source = formatter.format(
            ListStatementSource(
                statements = statements,
            ),
        )
        val formattedText = StringBuilder()

        while (true) {
            when (val result = source.nextFormattedStatement()) {
                is FormattedStatementReadResult.Success -> {
                    formattedText.append(result.formattedText)
                    source = result.remainingSource
                }

                is FormattedStatementReadResult.Failure ->
                    error("Unexpected formatting failure: ${result.error}")

                FormattedStatementReadResult.EndOfInput ->
                    return formattedText.toString()
            }
        }
    }

    private fun binaryExpression(left: Expression, operator: BinaryOperator, right: Expression): BinaryExpression {
        return BinaryExpression(
            left = left,
            operator = operator,
            operatorSpan = testSpan,
            right = right,
        )
    }

    private fun unaryExpression(operator: UnaryOperator, operand: Expression): UnaryExpression {
        return UnaryExpression(
            operator = operator,
            operatorSpan = testSpan,
            operand = operand,
        )
    }
}

private data object OverridingVariableDeclarationFormatter : StatementFormatter {

    override fun supportsStatement(statement: Statement): Boolean {
        return statement is VariableDeclarationStatement
    }

    override fun formatStatement(statement: Statement): StatementFormattingResult {
        assertIs<VariableDeclarationStatement>(statement)

        return StatementFormattingResult.Success(
            formattedText = "overridden",
        )
    }
}

private data object PipeSeparationPolicy : StatementSeparationPolicy {

    override fun separatorBeforeStatement(statement: Statement, hasPreviousStatement: Boolean): String {
        return if (hasPreviousStatement) "|" else ""
    }
}
