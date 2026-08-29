package printscript.cli.internal.operation

import printscript.cli.internal.io.DiscardedProgramOutput
import printscript.cli.internal.io.Terminal
import printscript.cli.internal.report.ErrorReporter
import printscript.v1.interpreter.PrintScriptV1ProgramOutput

internal class ValidationOperation(
    errorReporter: ErrorReporter,
) : InterpretingOperation(errorReporter) {

    override fun programOutputOn(terminal: Terminal): PrintScriptV1ProgramOutput {
        return DiscardedProgramOutput
    }

    override fun reportSuccessOn(terminal: Terminal) {
        terminal.writeLine("El archivo es válido.")
    }
}
