package printscript.cli.internal.command

import com.github.ajalt.clikt.core.Context
import printscript.cli.internal.operation.ExecutionOperation
import printscript.cli.internal.operation.SourceOperation
import printscript.cli.internal.operation.SourceOperationRequest
import printscript.cli.internal.report.ErrorReporter

internal class ExecutionCommand(
    errorReporter: ErrorReporter,
) : SourceFileOperationCommand(name = "execution", errorReporter = errorReporter) {

    override fun help(context: Context): String {
        return "Ejecuta el programa y muestra su salida"
    }

    override fun operationFor(request: SourceOperationRequest): SourceOperation {
        return ExecutionOperation(errorReporter)
    }
}
