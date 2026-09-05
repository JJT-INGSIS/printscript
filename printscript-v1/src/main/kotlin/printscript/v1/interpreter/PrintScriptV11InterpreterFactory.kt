package printscript.v1.interpreter

import printscript.interpreter.Interpreter
import printscript.interpreter.StatementExecutor
import printscript.runtime.Environment
import printscript.runtime.EnvironmentVariableProvider
import printscript.runtime.ExpressionEvaluator
import printscript.runtime.ProgramInput
import printscript.runtime.ProgramOutput
import printscript.v1.interpreter.internal.statement.IfExecutor

public object PrintScriptV11InterpreterFactory {

    @JvmStatic
    @JvmOverloads
    public fun create(
        output: ProgramOutput,
        input: ProgramInput,
        environmentVariables: EnvironmentVariableProvider,
        additionalStatementExecutors: List<StatementExecutor<Environment>> = emptyList(),
        expressionEvaluator: ExpressionEvaluator = PrintScriptV11ExpressionEvaluatorFactory.create(
            input = input,
            environmentVariables = environmentVariables,
        ),
    ): Interpreter {
        return PrintScriptV1InterpreterFactory.create(
            output = output,
            additionalStatementExecutors = additionalStatementExecutors + IfExecutor(),
            expressionEvaluator = expressionEvaluator,
        )
    }
}
