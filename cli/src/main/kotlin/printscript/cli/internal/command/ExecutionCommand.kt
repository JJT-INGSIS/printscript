package printscript.cli.internal.command

import printscript.cli.internal.arguments.CliArguments
import printscript.cli.internal.io.Terminal
import printscript.cli.internal.io.TerminalProgramOutput
import printscript.cli.internal.report.ErrorReporter
import printscript.interpreter.InterpretationResult
import printscript.interpreter.Interpreter
import printscript.interpreter.PrintScriptInterpreterFactory
import printscript.interpreter.output.ProgramOutput
import printscript.statement.StatementSource

internal class ExecutionCommand(
    private val errorReporter: ErrorReporter,
    private val createInterpreter: (ProgramOutput) -> Interpreter =
        PrintScriptInterpreterFactory::createV1,
) : CliCommand {

    override val operationName: String = "execution"

    override fun runOperation(
        arguments: CliArguments,
        statements: StatementSource,
        terminal: Terminal,
    ): CommandOutcome {
        val interpreter = createInterpreter(
            TerminalProgramOutput(terminal),
        )

        return when (val result = interpreter.interpret(statements)) {
            InterpretationResult.Success ->
                CommandOutcome.Success

            is InterpretationResult.ParseFailure ->
                CommandOutcome.Failure(
                    errorReporter.describe(result.error),
                )

            is InterpretationResult.SemanticFailure ->
                CommandOutcome.Failure(
                    errorReporter.describe(result.error),
                )
        }
    }
}
