package printscript.v1.interpreter

import printscript.runtime.ExpressionEvaluator
import printscript.v1.interpreter.internal.expression.DefaultExpressionEvaluator
import printscript.v1.interpreter.internal.expression.rule.printScriptV1ExpressionEvaluationRules

public object PrintScriptV1ExpressionEvaluatorFactory {

    @JvmStatic
    public fun create(): ExpressionEvaluator {
        return DefaultExpressionEvaluator(
            evaluationRules = printScriptV1ExpressionEvaluationRules(),
        )
    }
}
