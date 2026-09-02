package printscript.v1.interpreter

import printscript.runtime.ExpressionEvaluator
import printscript.v1.interpreter.internal.expression.DefaultExpressionEvaluator

public object PrintScriptV1ExpressionEvaluatorFactory {

    public fun create(): ExpressionEvaluator {
        return DefaultExpressionEvaluator()
    }
}
