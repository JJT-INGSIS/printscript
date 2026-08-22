package printscript.formatter.internal.statement

import printscript.ast.DeclaredType
import printscript.ast.expression.Expression
import printscript.ast.statement.Statement
import printscript.ast.statement.VariableDeclarationStatement
import printscript.formatter.FormattingError
import printscript.formatter.internal.expression.ExpressionFormatter

internal class DeclarationFormatter(
    private val expressionFormatter: ExpressionFormatter,
    private val spaceBeforeColon: Boolean,
    private val spaceAfterColon: Boolean,
) : StatementFormatter {

    override fun supportsStatement(
        statement: Statement,
    ): Boolean {
        return statement is VariableDeclarationStatement
    }

    override fun formatStatement(
        statement: Statement,
    ): StatementFormattingResult {
        if (statement !is VariableDeclarationStatement) {
            return unsupportedStatement(statement)
        }

        return StatementFormattingResult.Success(
            formattedText = formatDeclaration(statement),
        )
    }

    private fun formatDeclaration(
        statement: VariableDeclarationStatement,
    ): String {
        val spacingBeforeColon = spaceIfEnabled(spaceBeforeColon)
        val spacingAfterColon = spaceIfEnabled(spaceAfterColon)
        val declaredType = formatDeclaredType(
            statement.declaredType,
        )
        val formattedInitializer = formatInitializerClause(
            statement.initializer,
        )

        return "let ${statement.identifier.value}" +
                "$spacingBeforeColon:$spacingAfterColon$declaredType" +
                "$formattedInitializer;"
    }

    private fun formatDeclaredType(
        declaredType: DeclaredType,
    ): String {
        return when (declaredType) {
            DeclaredType.NUMBER -> "number"
            DeclaredType.STRING -> "string"
        }
    }

    private fun formatInitializerClause(
        initializer: Expression?,
    ): String {
        if (initializer == null) {
            return ""
        }

        val formattedExpression =
            expressionFormatter.formatExpression(initializer)

        return " = $formattedExpression"
    }

    private fun spaceIfEnabled(
        enabled: Boolean,
    ): String {
        return if (enabled) {
            " "
        } else {
            ""
        }
    }

    private fun unsupportedStatement(
        statement: Statement,
    ): StatementFormattingResult.Failure {
        return StatementFormattingResult.Failure(
            error = FormattingError.UnsupportedStatement(
                span = statement.span,
            ),
        )
    }
}
