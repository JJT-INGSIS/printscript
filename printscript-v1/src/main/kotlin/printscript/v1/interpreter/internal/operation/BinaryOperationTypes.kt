package printscript.v1.interpreter.internal.operation

import printscript.ast.DeclaredType
import printscript.ast.expression.BinaryOperator

internal fun BinaryOperator.resultType(left: DeclaredType, right: DeclaredType): DeclaredType? {
    return when (this) {
        BinaryOperator.ADD -> when {
            left == DeclaredType.STRING || right == DeclaredType.STRING -> DeclaredType.STRING
            else -> numericResultType(left, right)
        }

        BinaryOperator.SUBTRACT, BinaryOperator.MULTIPLY, BinaryOperator.DIVIDE -> numericResultType(left, right)
    }
}

private fun numericResultType(left: DeclaredType, right: DeclaredType): DeclaredType? {
    return DeclaredType.NUMBER.takeIf { left == it && right == it }
}
