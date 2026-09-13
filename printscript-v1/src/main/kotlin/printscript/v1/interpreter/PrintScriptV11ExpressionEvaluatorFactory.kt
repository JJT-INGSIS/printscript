package printscript.v1.interpreter

import printscript.runtime.EnvironmentVariableProvider
import printscript.runtime.ExpressionEvaluator
import printscript.runtime.ProgramInput
import printscript.v1.interpreter.internal.expression.PrintScriptV11ExpressionEvaluator

public object PrintScriptV11ExpressionEvaluatorFactory {

    @JvmStatic
    public fun create(input: ProgramInput, environmentVariables: EnvironmentVariableProvider): ExpressionEvaluator {
        return PrintScriptV11ExpressionEvaluator(
            input = input,
            environmentVariables = environmentVariables,
        )
    }
}
