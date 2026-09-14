package printscript.v1.validation

import printscript.ast.DeclaredType
import printscript.interpreter.StatementExecutor
import printscript.v1.validation.internal.StaticValidator
import printscript.v1.validation.internal.ValidationEnvironment
import printscript.v1.validation.internal.expression.ExpressionTypeResolver
import printscript.v1.validation.internal.expression.PrintScriptV1ExpressionTypeResolver
import printscript.v1.validation.internal.statement.AssignmentValidator
import printscript.v1.validation.internal.statement.DeclarationValidator
import printscript.v1.validation.internal.statement.PrintlnValidator

public object PrintScriptV1ValidatorFactory {

    private val printScriptV1DeclaredTypes: Set<DeclaredType> = setOf(
        DeclaredType.NUMBER,
        DeclaredType.STRING,
    )

    @JvmStatic
    public fun create(): Validator {
        return createWith(expressionTypes = PrintScriptV1ExpressionTypeResolver)
    }

    internal fun createWith(
        expressionTypes: ExpressionTypeResolver,
        additionalDeclaredTypes: Set<DeclaredType> = emptySet(),
        additionalValidators: List<StatementExecutor<ValidationEnvironment>> = emptyList(),
    ): Validator {
        return StaticValidator(
            additionalValidators + listOf(
                DeclarationValidator(expressionTypes, printScriptV1DeclaredTypes + additionalDeclaredTypes),
                AssignmentValidator(expressionTypes),
                PrintlnValidator(expressionTypes),
            ),
        )
    }
}
