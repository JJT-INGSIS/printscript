package printscript.v1.linter

import printscript.ast.DeclaredType
import printscript.ast.Identifier
import printscript.ast.expression.BinaryExpression
import printscript.ast.expression.BinaryOperator
import printscript.ast.expression.Expression
import printscript.ast.expression.GroupingExpression
import printscript.ast.expression.IdentifierExpression
import printscript.ast.expression.NumberLiteralExpression
import printscript.ast.expression.ReadEnvironmentExpression
import printscript.ast.expression.ReadInputExpression
import printscript.ast.expression.StringLiteralExpression
import printscript.ast.expression.StringQuoteStyle
import printscript.ast.expression.UnaryExpression
import printscript.ast.expression.UnaryOperator
import printscript.ast.statement.AssignmentStatement
import printscript.ast.statement.BlockStatement
import printscript.ast.statement.IfStatement
import printscript.ast.statement.PrintlnStatement
import printscript.ast.statement.VariableDeclarationStatement
import printscript.linter.Diagnostic
import printscript.linter.DiagnosticReadResult
import printscript.linter.DiagnosticSource
import printscript.linter.LintRule
import printscript.linter.Linter
import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan
import printscript.statement.Statement
import printscript.statement.StatementReadResult
import printscript.statement.StatementSource
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertIs

private const val CONSUMED_STATEMENT_COUNT = 1
private const val FIRST_LINE = 1
private const val FIRST_COLUMN = 1
private const val FIRST_OFFSET = 0L

internal val anySpan: SourceSpan = SourceSpan(
    start = SourcePosition(FIRST_LINE, FIRST_COLUMN, FIRST_OFFSET),
    end = SourcePosition(FIRST_LINE, FIRST_COLUMN, FIRST_OFFSET),
)

internal fun name(value: String): Identifier {
    return Identifier(
        value = value,
        span = anySpan,
    )
}

internal fun number(value: String): Expression {
    return NumberLiteralExpression(
        value = BigDecimal(value),
        span = anySpan,
    )
}

internal fun text(value: String): Expression {
    return StringLiteralExpression(
        value = value,
        quoteStyle = StringQuoteStyle.DOUBLE,
        span = anySpan,
    )
}

internal fun variable(value: String): Expression {
    return IdentifierExpression(
        identifier = name(value),
    )
}

internal fun sum(left: Expression, right: Expression): Expression {
    return BinaryExpression(
        left = left,
        operator = BinaryOperator.ADD,
        operatorSpan = anySpan,
        right = right,
    )
}

internal fun negated(operand: Expression): Expression {
    return UnaryExpression(
        operator = UnaryOperator.MINUS,
        operatorSpan = anySpan,
        operand = operand,
    )
}

internal fun grouped(expression: Expression): Expression {
    return GroupingExpression(
        expression = expression,
        span = anySpan,
    )
}

internal fun readInput(prompt: Expression): Expression {
    return ReadInputExpression(
        prompt = prompt,
        span = anySpan,
    )
}

internal fun readEnv(variableName: Expression): Expression {
    return ReadEnvironmentExpression(
        variableName = variableName,
        span = anySpan,
    )
}

internal fun declare(variableName: String, type: DeclaredType, initializer: Expression?): Statement {
    return VariableDeclarationStatement(
        identifier = name(variableName),
        declaredType = type,
        initializer = initializer,
        span = anySpan,
    )
}

internal fun assign(variableName: String, expression: Expression): Statement {
    return AssignmentStatement(
        target = name(variableName),
        expression = expression,
        span = anySpan,
    )
}

internal fun printOf(argument: Expression): Statement {
    return PrintlnStatement(
        argument = argument,
        span = anySpan,
    )
}

internal fun blockOf(vararg statements: Statement): BlockStatement {
    return BlockStatement(
        statements = statements.toList(),
        span = anySpan,
    )
}

internal fun ifStatement(thenBranch: BlockStatement, elseBranch: BlockStatement? = null): Statement {
    return IfStatement(
        condition = name("active"),
        thenBranch = thenBranch,
        elseBranch = elseBranch,
        span = anySpan,
    )
}

internal data class ExtensionStatement(
    override val span: SourceSpan = anySpan,
) : Statement

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

internal fun identifierNamingRule(
    convention: PrintScriptV1NamingConvention = PrintScriptV1NamingConvention.CAMEL_CASE,
): PrintScriptV1RuleConfiguration {
    return PrintScriptV1RuleConfiguration.IdentifierNaming(convention)
}

internal fun printlnArgumentRule(): PrintScriptV1RuleConfiguration {
    return PrintScriptV1RuleConfiguration.PrintlnArgument(
        acceptanceByKind = mapOf(
            PrintScriptV1ExpressionKind.LITERAL to PrintScriptV1ArgumentAcceptance.ACCEPTED,
            PrintScriptV1ExpressionKind.VARIABLE to PrintScriptV1ArgumentAcceptance.ACCEPTED,
            PrintScriptV1ExpressionKind.COMPOSED to PrintScriptV1ArgumentAcceptance.REJECTED,
        ),
    )
}

internal fun linterWith(vararg rules: PrintScriptV1RuleConfiguration): Linter {
    return PrintScriptV1LinterFactory.create(
        configuration = PrintScriptV1LinterConfiguration(
            rules = rules.toList(),
        ),
    )
}

internal fun readInputArgumentRule(): PrintScriptV1RuleConfiguration {
    return PrintScriptV1RuleConfiguration.ReadInputArgument(
        acceptanceByKind = mapOf(
            PrintScriptV1ExpressionKind.LITERAL to PrintScriptV1ArgumentAcceptance.ACCEPTED,
            PrintScriptV1ExpressionKind.VARIABLE to PrintScriptV1ArgumentAcceptance.ACCEPTED,
            PrintScriptV1ExpressionKind.COMPOSED to PrintScriptV1ArgumentAcceptance.REJECTED,
        ),
    )
}

internal fun v11LinterWith(
    rules: List<PrintScriptV1RuleConfiguration> = emptyList(),
    additionalRules: List<LintRule> = emptyList(),
): Linter {
    return PrintScriptV11LinterFactory.create(
        configuration = PrintScriptV11LinterConfiguration(rules = rules),
        additionalRules = additionalRules,
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

internal fun diagnosticsOf(linter: Linter, vararg statements: Statement): List<Diagnostic> {
    return linter.lint(
        source = ListStatementSource(statements.toList()),
    ).readAll()
}

internal fun List<Diagnostic>.assertNoDiagnostics() {
    assertEquals(
        expected = emptyList(),
        actual = this,
    )
}

internal fun List<Diagnostic>.assertNamingViolations(vararg expectedNames: String) {
    assertEquals(
        expected = expectedNames.toList(),
        actual = map { diagnostic ->
            assertIs<PrintScriptV1Diagnostic.NamingConventionViolation>(diagnostic)
                .identifier
                .value
        },
    )
}
