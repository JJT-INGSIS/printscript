package printscript.interpreter.operations

import printscript.ast.expression.BinaryOperator

internal class BinaryOperationRegistry {

    private val operations: Map<BinaryOperator, BinaryOperation> = mapOf(
        BinaryOperator.ADD to AddOperation(),
        BinaryOperator.SUBTRACT to SubtractOperation(),
        BinaryOperator.MULTIPLY to MultiplyOperation(),
        BinaryOperator.DIVIDE to DivideOperation(),
    )

    fun forOperator(operator: BinaryOperator): BinaryOperation? {
        return operations[operator]
    }
}