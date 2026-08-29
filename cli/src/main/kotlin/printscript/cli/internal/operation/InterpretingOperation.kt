package printscript.cli.internal.operation

import printscript.cli.internal.io.Terminal
import printscript.cli.internal.report.ErrorReporter
import printscript.interpreter.InterpretationResult
import printscript.interpreter.Interpreter
import printscript.statement.StatementSource
import printscript.v1.interpreter.PrintScriptV1InterpreterFactory
import printscript.v1.interpreter.PrintScriptV1ProgramOutput

internal abstract class InterpretingOperation(
    private val errorReporter: ErrorReporter,
    private val createInterpreter: (PrintScriptV1ProgramOutput) -> Interpreter = { output ->
        PrintScriptV1InterpreterFactory.create(output)
    },
) : SourceOperation {

    protected abstract fun programOutputOn(terminal: Terminal): PrintScriptV1ProgramOutput

    protected open fun reportSuccessOn(terminal: Terminal) = Unit

    final override fun outcomeFor(statements: StatementSource, terminal: Terminal): OperationOutcome {
        val interpreter = createInterpreter(
            programOutputOn(terminal),
        )

        return when (val result = interpreter.interpret(statements)) {
            InterpretationResult.Success -> {
                reportSuccessOn(terminal)

                OperationOutcome.Success
            }

            is InterpretationResult.ParseFailure ->
                OperationOutcome.Failure(
                    errorReporter.describe(result.error),
                )

            is InterpretationResult.SemanticFailure ->
                OperationOutcome.Failure(
                    errorReporter.describe(result.error),
                )
        }
    }
}
