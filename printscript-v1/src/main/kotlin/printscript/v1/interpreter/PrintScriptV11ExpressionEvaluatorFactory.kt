package printscript.v1.interpreter

import printscript.runtime.EnvironmentVariableProvider
import printscript.runtime.ExpressionEvaluator
import printscript.runtime.ProgramInput
import printscript.v1.interpreter.internal.expression.DefaultExpressionEvaluator
import printscript.v1.interpreter.internal.expression.PrintScriptV11ExpressionEvaluation

public object PrintScriptV11ExpressionEvaluatorFactory {

    @JvmStatic
    public fun create(input: ProgramInput, environmentVariables: EnvironmentVariableProvider): ExpressionEvaluator {
        return DefaultExpressionEvaluator(
            v11ExpressionEvaluation = PrintScriptV11ExpressionEvaluation(
                input = input,
                environmentVariables = environmentVariables,
            ),
        )
    }
}
