package printscript.interpreter

import printscript.interpreter.environment.Environment
import printscript.interpreter.expressions.ExpressionEvaluator
import printscript.interpreter.output.ProgramOutput
import printscript.interpreter.statements.StatementExecutor
import printscript.interpreter.statements.StatementExecutorDispatcher
import printscript.interpreter.value.RuntimeValue
import printscript.model.ast.expression.Expression
import printscript.model.ast.statement.Statement
import printscript.statement.StatementReadResult
import printscript.statement.StatementSource

internal class PrintScriptInterpreter(
    private val output: ProgramOutput,
    override val environment: Environment,
    statementExecutors: List<StatementExecutor>,
) : Interpreter, ExecutionContext {

    private val expressionEvaluator =
        ExpressionEvaluator(environment)

    private val statementExecutorDispatcher =
        StatementExecutorDispatcher(
            executors = statementExecutors.toList(),
        )

    override fun interpret(
        source: StatementSource,
    ): InterpretationResult {
        while (true) {
            return interpretNext(source) ?: continue
        }
    }

    private fun interpretNext(
        source: StatementSource,
    ): InterpretationResult? =
        when (val readResult = source.nextStatement()) {
            StatementReadResult.EndOfInput -> InterpretationResult.Success

            is StatementReadResult.Failure -> InterpretationResult.ParseFailure(
                error = readResult.error,
            )

            is StatementReadResult.Success -> executeStatement(readResult.statement)
        }

    private fun executeStatement(
        statement: Statement,
    ): InterpretationResult? =
        when (
            val executionResult = statementExecutorDispatcher.execute(
                statement = statement,
                context = this,
            )
        ) {
            is ExecutionResult.Success -> null

            is ExecutionResult.Failure -> InterpretationResult.SemanticFailure(
                error = executionResult.error,
            )
        }

    override fun evaluate(
        expression: Expression,
    ): ExecutionResult<RuntimeValue> {
        return expressionEvaluator.evaluate(expression)
    }

    override fun emit(
        line: String,
    ) {
        output.emit(line)
    }
}