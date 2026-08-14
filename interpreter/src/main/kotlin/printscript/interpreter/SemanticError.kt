package printscript.interpreter

import printscript.model.ast.DeclaredType
import printscript.model.ast.expression.BinaryOperator
import printscript.model.ast.expression.UnaryOperator
import printscript.model.source.SourceSpan

sealed interface SemanticError {
    val span: SourceSpan

    data class UndeclaredVariable(
        val name: String,
        override val span: SourceSpan,
    ) : SemanticError

    data class UninitializedVariable(
        val name: String,
        override val span: SourceSpan,
    ) : SemanticError

    data class AlreadyDeclaredVariable(
        val name: String,
        override val span: SourceSpan,
    ) : SemanticError

    data class TypeMismatch(
        val name: String,
        val expected: DeclaredType,
        val actual: DeclaredType,
        override val span: SourceSpan,
    ) : SemanticError

    data class InvalidBinaryOperands(
        val operator: BinaryOperator,
        val left: DeclaredType,
        val right: DeclaredType,
        override val span: SourceSpan,
    ) : SemanticError

    data class InvalidUnaryOperand(
        val operator: UnaryOperator,
        val operand: DeclaredType,
        override val span: SourceSpan,
    ) : SemanticError

    data class DivisionByZero(
        override val span: SourceSpan,
    ) : SemanticError

    data class UnsupportedBinaryOperator(
        val operator: BinaryOperator,
        override val span: SourceSpan,
    ) : SemanticError

    data class UnsupportedStatement(
        override val span: SourceSpan,
    ) : SemanticError
}