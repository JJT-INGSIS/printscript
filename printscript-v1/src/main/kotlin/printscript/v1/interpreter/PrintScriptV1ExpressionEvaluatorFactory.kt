package printscript.v1.interpreter

import printscript.v1.interpreter.internal.expression.DefaultExpressionEvaluator

public object PrintScriptV1ExpressionEvaluatorFactory {

    public fun create(): PrintScriptV1ExpressionEvaluator {
        return DefaultExpressionEvaluator()
    }
}
