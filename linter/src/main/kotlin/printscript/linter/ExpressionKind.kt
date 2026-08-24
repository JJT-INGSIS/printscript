package printscript.linter

import printscript.ast.expression.BinaryExpression
import printscript.ast.expression.Expression
import printscript.ast.expression.GroupingExpression
import printscript.ast.expression.IdentifierExpression
import printscript.ast.expression.NumberLiteralExpression
import printscript.ast.expression.StringLiteralExpression
import printscript.ast.expression.UnaryExpression

/**
 * Clasifica una expresión sin decidir nada sobre ella: qué se acepta y
 * qué no es política, y vive en la configuración.
 */
public enum class ExpressionKind {
    LITERAL,
    VARIABLE,
    COMPOSED,
    ;

    public companion object {

        public fun of(
            expression: Expression,
        ): ExpressionKind {
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
