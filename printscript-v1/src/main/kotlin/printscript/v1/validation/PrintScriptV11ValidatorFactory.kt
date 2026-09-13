package printscript.v1.validation

import printscript.ast.DeclaredType
import printscript.v1.validation.internal.expression.ExpressionTypeResolver
import printscript.v1.validation.internal.expression.rule.printScriptV11ExpressionTypeRules
import printscript.v1.validation.internal.statement.IfValidator

public object PrintScriptV11ValidatorFactory {

    @JvmStatic
    public fun create(): Validator {
        return PrintScriptV1ValidatorFactory.createWith(
            expressionTypes = ExpressionTypeResolver(printScriptV11ExpressionTypeRules()),
            supportedDeclaredTypes = DeclaredType.entries.toSet(),
            additionalValidators = listOf(IfValidator()),
        )
    }
}
