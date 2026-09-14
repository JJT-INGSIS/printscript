package printscript.v1.validation

import printscript.ast.DeclaredType
import printscript.v1.validation.internal.expression.PrintScriptV11ExpressionTypeResolver
import printscript.v1.validation.internal.statement.IfValidator

public object PrintScriptV11ValidatorFactory {

    @JvmStatic
    public fun create(): Validator {
        return PrintScriptV1ValidatorFactory.createWith(
            expressionTypes = PrintScriptV11ExpressionTypeResolver,
            additionalDeclaredTypes = setOf(DeclaredType.BOOLEAN),
            additionalValidators = listOf(IfValidator()),
        )
    }
}
