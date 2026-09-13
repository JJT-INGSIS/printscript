package printscript.v1.interpreter

import printscript.runtime.EnvironmentVariableProvider
import printscript.runtime.ExpressionEvaluator
import printscript.runtime.ProgramInput
import printscript.v1.interpreter.internal.expression.DefaultExpressionEvaluator
import printscript.v1.interpreter.internal.expression.rule.printScriptV11ExpressionEvaluationRules

public object PrintScriptV11ExpressionEvaluatorFactory {

    @JvmStatic
    public fun create(input: ProgramInput, environmentVariables: EnvironmentVariableProvider): ExpressionEvaluator {
        return DefaultExpressionEvaluator(
            evaluationRules = printScriptV11ExpressionEvaluationRules(
                input = input,
                environmentVariables = environmentVariables,
            ),
        )
    }
}
