package printscript.v1.validation.internal.expression

import printscript.ast.DeclaredType
import printscript.ast.expression.Expression
import printscript.interpreter.ExecutionResult
import printscript.v1.validation.internal.ValidationEnvironment

internal interface ExpressionTypeResolver {

    fun typeOf(
        expression: Expression,
        environment: ValidationEnvironment,
        expectedType: DeclaredType? = null,
    ): ExecutionResult<DeclaredType>
}
