package printscript.v1.validation

import printscript.ast.DeclaredType
import printscript.interpreter.StatementExecutor
import printscript.v1.validation.internal.StaticValidator
import printscript.v1.validation.internal.ValidationEnvironment
import printscript.v1.validation.internal.expression.ExpressionTypeResolver
import printscript.v1.validation.internal.statement.AssignmentValidator
import printscript.v1.validation.internal.statement.DeclarationValidator
import printscript.v1.validation.internal.statement.PrintlnValidator

public object PrintScriptV1ValidatorFactory {

    @JvmStatic
    public fun create(): Validator {
        return createWith(ExpressionTypeResolver(setOf(DeclaredType.NUMBER, DeclaredType.STRING)))
    }

    internal fun createWith(
        expressionTypes: ExpressionTypeResolver,
        additionalValidators: List<StatementExecutor<ValidationEnvironment>> = emptyList(),
    ): Validator {
        return StaticValidator(
            additionalValidators + listOf(
                DeclarationValidator(expressionTypes),
                AssignmentValidator(expressionTypes),
                PrintlnValidator(expressionTypes),
            ),
        )
    }
}
