package printscript.v1.interpreter.internal.operation

import printscript.ast.expression.BinaryOperator

internal class BinaryOperationRegistry {

    private val operations: Map<BinaryOperator, BinaryOperation> = mapOf(
        BinaryOperator.ADD to AddOperation(),
        BinaryOperator.SUBTRACT to NumericBinaryOperation(
            operator = BinaryOperator.SUBTRACT,
            calculation = SubtractCalculation(),
        ),
        BinaryOperator.MULTIPLY to NumericBinaryOperation(
            operator = BinaryOperator.MULTIPLY,
            calculation = MultiplyCalculation(),
        ),
        BinaryOperator.DIVIDE to NumericBinaryOperation(
            operator = BinaryOperator.DIVIDE,
            calculation = DivideCalculation(),
        ),
    )

    fun forOperator(operator: BinaryOperator): BinaryOperation? {
        return operations[operator]
    }
}
