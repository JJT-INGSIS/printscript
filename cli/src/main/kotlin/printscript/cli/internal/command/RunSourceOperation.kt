package printscript.cli.internal.command

import com.github.ajalt.clikt.core.CliktCommand
import printscript.cli.internal.SourceOperationRunner
import printscript.cli.internal.operation.SourceOperationFactory
import printscript.cli.internal.operation.SourceOperationRequest
import printscript.cli.internal.pipeline.StatementSourcePipeline
import printscript.cli.internal.report.ErrorReporter

internal fun CliktCommand.runSourceOperation(
    request: SourceOperationRequest,
    operationFactory: SourceOperationFactory,
    errorReporter: ErrorReporter,
    pipeline: StatementSourcePipeline,
) {
    val exitCode = SourceOperationRunner(
        terminal = EchoTerminal(this),
        pipeline = pipeline,
        errorReporter = errorReporter,
    ).exitCodeFor(
        operation = operationFactory.create(request),
        request = request,
    )

    ProgramTermination.endWith(exitCode)
}
