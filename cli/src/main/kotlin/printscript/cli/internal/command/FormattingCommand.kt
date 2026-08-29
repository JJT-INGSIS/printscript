package printscript.cli.internal.command

import com.github.ajalt.clikt.core.Context
import printscript.cli.internal.operation.FormattingOperation
import printscript.cli.internal.operation.SourceOperation
import printscript.cli.internal.operation.SourceOperationRequest
import printscript.cli.internal.report.ErrorReporter

internal class FormattingCommand(
    errorReporter: ErrorReporter,
) : SourceFileOperationCommand(name = "formatting", errorReporter = errorReporter) {

    override fun help(context: Context): String {
        return "Reescribe el código con el formato configurado"
    }

    override fun operationFor(request: SourceOperationRequest): SourceOperation {
        return FormattingOperation(errorReporter)
    }
}
