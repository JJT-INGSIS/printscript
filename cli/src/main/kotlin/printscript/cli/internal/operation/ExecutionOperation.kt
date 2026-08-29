package printscript.cli.internal.operation

import printscript.cli.internal.io.Terminal
import printscript.cli.internal.io.TerminalProgramOutput
import printscript.cli.internal.report.ErrorReporter
import printscript.v1.interpreter.PrintScriptV1ProgramOutput

internal class ExecutionOperation(
    errorReporter: ErrorReporter,
) : InterpretingOperation(errorReporter) {

    override fun programOutputOn(terminal: Terminal): PrintScriptV1ProgramOutput {
        return TerminalProgramOutput(terminal)
    }
}
