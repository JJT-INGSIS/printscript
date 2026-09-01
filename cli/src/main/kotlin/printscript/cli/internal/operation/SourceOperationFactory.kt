package printscript.cli.internal.operation

internal fun interface SourceOperationFactory {

    fun create(request: SourceOperationRequest): SourceOperation
}
