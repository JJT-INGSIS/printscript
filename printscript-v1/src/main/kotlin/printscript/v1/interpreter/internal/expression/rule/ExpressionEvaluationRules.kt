package printscript.v1.interpreter.internal.expression.rule

import printscript.runtime.EnvironmentVariableProvider
import printscript.runtime.ProgramInput

internal fun printScriptV1ExpressionEvaluationRules(): List<ExpressionEvaluationRule> {
    return listOf(
        NumberLiteralEvaluationRule,
        StringLiteralEvaluationRule,
        GroupingEvaluationRule,
        IdentifierEvaluationRule,
        UnaryOperationEvaluationRule,
        BinaryOperationEvaluationRule(),
    )
}

internal fun printScriptV11ExpressionEvaluationRules(
    input: ProgramInput,
    environmentVariables: EnvironmentVariableProvider,
): List<ExpressionEvaluationRule> {
    return printScriptV1ExpressionEvaluationRules() +
        listOf(
            BooleanLiteralEvaluationRule,
            ReadInputEvaluationRule(input = input),
            ReadEnvironmentEvaluationRule(environmentVariables = environmentVariables),
        )
}
