package printscript.v1.linter

import printscript.ast.expression.BinaryExpression
import printscript.ast.expression.Expression
import printscript.ast.expression.GroupingExpression
import printscript.ast.expression.IdentifierExpression
import printscript.ast.expression.NumberLiteralExpression
import printscript.ast.expression.StringLiteralExpression
import printscript.ast.expression.UnaryExpression

public enum class PrintScriptV1ExpressionKind {
    LITERAL,
    VARIABLE,
    COMPOSED,
    ;

    public companion object {

        public fun of(expression: Expression): PrintScriptV1ExpressionKind {
            return when (expression) {
                is NumberLiteralExpression -> LITERAL

                is StringLiteralExpression -> LITERAL

                is IdentifierExpression -> VARIABLE

                is BinaryExpression -> COMPOSED

                is UnaryExpression -> COMPOSED

                is GroupingExpression -> COMPOSED
            }
        }
    }
}
