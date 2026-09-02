package printscript.cli.internal.operation

import printscript.cli.internal.io.DiscardedProgramOutput
import printscript.cli.internal.io.Terminal
import printscript.cli.internal.report.ErrorReporter
import printscript.runtime.ProgramOutput

internal class ValidationOperation(
    errorReporter: ErrorReporter,
) : InterpretingOperation(errorReporter) {

    override fun programOutputOn(terminal: Terminal): ProgramOutput {
        return DiscardedProgramOutput
    }

    override fun reportSuccessOn(terminal: Terminal) {
        terminal.writeLine("El archivo es válido.")
    }
}
