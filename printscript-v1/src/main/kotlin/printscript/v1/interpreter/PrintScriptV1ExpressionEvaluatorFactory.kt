package printscript.v1.interpreter

import printscript.runtime.ExpressionEvaluator
import printscript.v1.interpreter.internal.expression.PrintScriptV1ExpressionEvaluator

public object PrintScriptV1ExpressionEvaluatorFactory {

    @JvmStatic
    public fun create(): ExpressionEvaluator {
        return PrintScriptV1ExpressionEvaluator
    }
}
