package printscript.cli.internal.operation

import printscript.cli.internal.io.Terminal
import printscript.cli.internal.io.TerminalProgramOutput
import printscript.cli.internal.report.ErrorReporter
import printscript.runtime.ProgramOutput

internal class ExecutionOperation(
    errorReporter: ErrorReporter,
) : InterpretingOperation(errorReporter) {

    override fun programOutputOn(terminal: Terminal): ProgramOutput {
        return TerminalProgramOutput(terminal)
    }
}
