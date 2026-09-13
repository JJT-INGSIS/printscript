package printscript.v1.validation.internal.expression.rule

internal fun printScriptV1ExpressionTypeRules(): List<ExpressionTypeRule> {
    return listOf(
        NumberLiteralTypeRule,
        StringLiteralTypeRule,
        IdentifierTypeRule,
        GroupingTypeRule,
        UnaryOperationTypeRule,
        BinaryOperationTypeRule,
    )
}

internal fun printScriptV11ExpressionTypeRules(): List<ExpressionTypeRule> {
    return printScriptV1ExpressionTypeRules() +
        listOf(
            BooleanLiteralTypeRule,
            ReadInputTypeRule,
            ReadEnvironmentTypeRule,
        )
}
