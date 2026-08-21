package printscript.interpreter

import printscript.ast.DeclaredType
import printscript.ast.expression.BinaryOperator
import printscript.ast.expression.UnaryOperator
import printscript.model.source.SourceSpan

public sealed interface SemanticError {

    public val span: SourceSpan

    public data class UndeclaredVariable(
        public val name: String,
        override val span: SourceSpan,
    ) : SemanticError

    public data class UninitializedVariable(
        public val name: String,
        override val span: SourceSpan,
    ) : SemanticError

    public data class AlreadyDeclaredVariable(
        public val name: String,
        override val span: SourceSpan,
    ) : SemanticError

    public data class TypeMismatch(
        public val name: String,
        public val expected: DeclaredType,
        public val actual: DeclaredType,
        override val span: SourceSpan,
    ) : SemanticError

    public data class InvalidBinaryOperands(
        public val operator: BinaryOperator,
        public val left: DeclaredType,
        public val right: DeclaredType,
        override val span: SourceSpan,
    ) : SemanticError

    public data class InvalidUnaryOperand(
        public val operator: UnaryOperator,
        public val operand: DeclaredType,
        override val span: SourceSpan,
    ) : SemanticError

    public data class DivisionByZero(
        override val span: SourceSpan,
    ) : SemanticError

    public data class UnsupportedBinaryOperator(
        public val operator: BinaryOperator,
        override val span: SourceSpan,
    ) : SemanticError

    public data class UnsupportedStatement(
        override val span: SourceSpan,
    ) : SemanticError
}
