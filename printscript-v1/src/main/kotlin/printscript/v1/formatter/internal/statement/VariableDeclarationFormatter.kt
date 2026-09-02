package printscript.v1.formatter.internal.statement

import printscript.ast.DeclaredType
import printscript.ast.expression.Expression
import printscript.ast.statement.VariableDeclarationStatement
import printscript.formatter.StatementFormatter
import printscript.formatter.StatementFormattingContext
import printscript.formatter.StatementFormattingResult
import printscript.statement.Statement
import printscript.v1.formatter.internal.expression.ExpressionFormatter
import printscript.v1.formatter.internal.spaceIfEnabled
import printscript.v1.internal.PrintScriptV1Lexemes.ASSIGNMENT_OPERATOR
import printscript.v1.internal.PrintScriptV1Lexemes.COLON
import printscript.v1.internal.PrintScriptV1Lexemes.DECLARATION_KEYWORD
import printscript.v1.internal.PrintScriptV1Lexemes.NUMBER_TYPE_NAME
import printscript.v1.internal.PrintScriptV1Lexemes.SEMICOLON
import printscript.v1.internal.PrintScriptV1Lexemes.STRING_TYPE_NAME

internal class VariableDeclarationFormatter(
    private val expressionFormatter: ExpressionFormatter,
    private val insertSpaceBeforeColon: Boolean,
    private val insertSpaceAfterColon: Boolean,
    private val insertSpaceAroundEqualsOperator: Boolean,
) : StatementFormatter {

    override fun supportsStatement(statement: Statement): Boolean {
        return statement is VariableDeclarationStatement
    }

    override fun formatStatement(
        statement: Statement,
        context: StatementFormattingContext,
    ): StatementFormattingResult {
        if (statement !is VariableDeclarationStatement) {
            return unsupportedStatementFailure(statement)
        }

        return StatementFormattingResult.Success(
            formattedText = formatDeclaration(statement),
        )
    }

    private fun formatDeclaration(statement: VariableDeclarationStatement): String {
        val spacingBeforeColon = spaceIfEnabled(insertSpaceBeforeColon)
        val spacingAfterColon = spaceIfEnabled(insertSpaceAfterColon)
        val formattedDeclaredType = formatDeclaredType(statement.declaredType)
        val formattedInitializer = formatInitializerClause(statement.initializer)

        return "$DECLARATION_KEYWORD ${statement.identifier.value}" +
            "$spacingBeforeColon$COLON$spacingAfterColon$formattedDeclaredType" +
            "$formattedInitializer$SEMICOLON"
    }

    private fun formatDeclaredType(declaredType: DeclaredType): String {
        return when (declaredType) {
            DeclaredType.NUMBER -> NUMBER_TYPE_NAME
            DeclaredType.STRING -> STRING_TYPE_NAME
        }
    }

    private fun formatInitializerClause(initializerExpression: Expression?): String {
        if (initializerExpression == null) {
            return ""
        }

        val formattedExpression = expressionFormatter.formatExpression(initializerExpression)
        val operatorSpacing = spaceIfEnabled(insertSpaceAroundEqualsOperator)

        return "$operatorSpacing$ASSIGNMENT_OPERATOR$operatorSpacing$formattedExpression"
    }
}
