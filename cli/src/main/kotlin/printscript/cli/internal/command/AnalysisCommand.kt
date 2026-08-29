package printscript.cli.internal.command

import com.github.ajalt.clikt.core.Context
import printscript.cli.internal.operation.AnalysisOperation
import printscript.cli.internal.operation.SourceOperation
import printscript.cli.internal.operation.SourceOperationRequest
import printscript.cli.internal.report.DiagnosticReporter
import printscript.cli.internal.report.ErrorReporter

internal class AnalysisCommand(
    errorReporter: ErrorReporter,
    private val diagnosticReporter: DiagnosticReporter,
) : SourceFileOperationCommand(name = "analyzing", errorReporter = errorReporter) {

    override fun help(context: Context): String {
        return "Reporta problemas de estilo sin modificar el archivo"
    }

    override fun operationFor(request: SourceOperationRequest): SourceOperation {
        return AnalysisOperation(errorReporter, diagnosticReporter)
    }
}
