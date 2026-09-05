package printscript.v1.interpreter

import printscript.ast.DeclaredType
import printscript.ast.expression.BinaryOperator
import printscript.ast.expression.UnaryOperator
import printscript.interpreter.SemanticError
import printscript.model.source.SourceSpan

public sealed interface PrintScriptV1SemanticError : SemanticError {

    public data class UndeclaredVariable(
        public val name: String,
        override val span: SourceSpan,
    ) : PrintScriptV1SemanticError

    public data class UninitializedVariable(
        public val name: String,
        override val span: SourceSpan,
    ) : PrintScriptV1SemanticError

    public data class AlreadyDeclaredVariable(
        public val name: String,
        override val span: SourceSpan,
    ) : PrintScriptV1SemanticError

    public data class ConstantReassignment(
        public val name: String,
        override val span: SourceSpan,
    ) : PrintScriptV1SemanticError

    public data class TypeMismatch(
        public val name: String,
        public val expected: DeclaredType,
        public val actual: DeclaredType,
        override val span: SourceSpan,
    ) : PrintScriptV1SemanticError

    public data class InvalidBinaryOperands(
        public val operator: BinaryOperator,
        public val left: DeclaredType,
        public val right: DeclaredType,
        override val span: SourceSpan,
    ) : PrintScriptV1SemanticError

    public data class InvalidUnaryOperand(
        public val operator: UnaryOperator,
        public val operand: DeclaredType,
        override val span: SourceSpan,
    ) : PrintScriptV1SemanticError

    public data class DivisionByZero(
        override val span: SourceSpan,
    ) : PrintScriptV1SemanticError

    public data class UnsupportedBinaryOperator(
        public val operator: BinaryOperator,
        override val span: SourceSpan,
    ) : PrintScriptV1SemanticError

    public data class InvalidIfCondition(
        public val name: String,
        public val actual: DeclaredType,
        override val span: SourceSpan,
    ) : PrintScriptV1SemanticError

    public data class InvalidInputPrompt(
        public val actual: DeclaredType,
        override val span: SourceSpan,
    ) : PrintScriptV1SemanticError

    public data class InputUnavailable(
        override val span: SourceSpan,
    ) : PrintScriptV1SemanticError

    public data class InvalidInputValue(
        public val expected: DeclaredType,
        override val span: SourceSpan,
    ) : PrintScriptV1SemanticError

    public data class InvalidEnvironmentVariableName(
        public val actual: DeclaredType,
        override val span: SourceSpan,
    ) : PrintScriptV1SemanticError

    public data class EnvironmentVariableNotFound(
        public val name: String,
        override val span: SourceSpan,
    ) : PrintScriptV1SemanticError

    public data class InvalidEnvironmentVariableValue(
        public val name: String,
        public val expected: DeclaredType,
        override val span: SourceSpan,
    ) : PrintScriptV1SemanticError
}
