package printscript.interpreter

import printscript.ast.expression.UnaryOperator
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
        val expected: printscript.ast.DeclaredType,
        val actual: printscript.ast.DeclaredType,
        override val span: SourceSpan,
    ) : SemanticError

    data class InvalidBinaryOperands(
        val operator: printscript.ast.expression.BinaryOperator,
        val left: printscript.ast.DeclaredType,
        val right: printscript.ast.DeclaredType,
        override val span: SourceSpan,
    ) : SemanticError

    data class InvalidUnaryOperand(
        val operator: UnaryOperator,
        val operand: printscript.ast.DeclaredType,
        override val span: SourceSpan,
    ) : SemanticError

    data class DivisionByZero(
        override val span: SourceSpan,
    ) : SemanticError

    data class UnsupportedBinaryOperator(
        val operator: printscript.ast.expression.BinaryOperator,
        override val span: SourceSpan,
    ) : SemanticError

    data class UnsupportedStatement(
        override val span: SourceSpan,
    ) : SemanticError
}