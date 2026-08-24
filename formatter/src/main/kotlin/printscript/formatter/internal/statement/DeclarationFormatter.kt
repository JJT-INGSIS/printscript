package printscript.formatter.internal.statement

import printscript.ast.DeclaredType
import printscript.ast.expression.Expression
import printscript.ast.statement.Statement
import printscript.ast.statement.VariableDeclarationStatement
import printscript.formatter.internal.expression.ExpressionFormatter

internal class DeclarationFormatter(
    private val expressionFormatter: ExpressionFormatter,
    private val insertSpaceBeforeColon: Boolean,
    private val insertSpaceAfterColon: Boolean,
    private val insertSpaceAroundEqualsOperator: Boolean,
) : StatementFormatter {

    override fun supportsStatement(statement: Statement): Boolean {
        return statement is VariableDeclarationStatement
    }

    override fun formatStatement(statement: Statement): StatementFormattingResult {
        if (statement !is VariableDeclarationStatement) {
            return createUnsupportedStatementFailure(statement)
        }

        return StatementFormattingResult.Success(
            formattedText = formatDeclaration(statement),
        )
    }

    private fun formatDeclaration(statement: VariableDeclarationStatement): String {
        val spacingBeforeColon =
            spaceIfEnabled(insertSpaceBeforeColon)
        val spacingAfterColon =
            spaceIfEnabled(insertSpaceAfterColon)
        val formattedDeclaredType = formatDeclaredType(
            statement.declaredType,
        )
        val formattedInitializer = formatInitializerClause(
            statement.initializer,
        )

        return "let ${statement.identifier.value}" +
            "$spacingBeforeColon:$spacingAfterColon$formattedDeclaredType" +
            "$formattedInitializer;"
    }

    private fun formatDeclaredType(declaredType: DeclaredType): String {
        return when (declaredType) {
            DeclaredType.NUMBER -> "number"
            DeclaredType.STRING -> "string"
        }
    }

    private fun formatInitializerClause(initializerExpression: Expression?): String {
        if (initializerExpression == null) {
            return ""
        }

        val formattedExpression =
            expressionFormatter.formatExpression(
                initializerExpression,
            )
        val equalsOperatorSpacing =
            spaceIfEnabled(insertSpaceAroundEqualsOperator)

        return "$equalsOperatorSpacing=" +
            "$equalsOperatorSpacing$formattedExpression"
    }

    private fun spaceIfEnabled(enabled: Boolean): String {
        return if (enabled) {
            " "
        } else {
            ""
        }
    }
}
