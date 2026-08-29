package printscript.cli.internal.command

import com.github.ajalt.clikt.core.Context
import printscript.cli.internal.operation.SourceOperation
import printscript.cli.internal.operation.SourceOperationRequest
import printscript.cli.internal.operation.ValidationOperation
import printscript.cli.internal.report.ErrorReporter

internal class ValidationCommand(
    errorReporter: ErrorReporter,
) : SourceFileOperationCommand(name = "validation", errorReporter = errorReporter) {

    override fun help(context: Context): String {
        return "Verifica que el archivo sea válido, sin mostrar lo que el programa imprimiría"
    }

    override fun operationFor(request: SourceOperationRequest): SourceOperation {
        return ValidationOperation(errorReporter)
    }
}
