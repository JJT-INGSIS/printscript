package printscript.cli.internal.operation

/**
 * Asocia el nombre público de cada operación con su implementación.
 *
 * El nombre vive acá y no adentro de la operación porque es vocabulario
 * de la línea de comandos: la misma [SourceOperation] podría exponerse
 * con otro nombre sin cambiar una línea de su lógica.
 *
 * Reemplaza a `CommandDispatcher`, que derivaba las claves de un campo
 * `operationName` que las operaciones ya no tienen.
 */
internal class SourceOperationRegistry(
    operationsByName: Map<String, SourceOperation>,
) {

    private val operationsByName: Map<String, SourceOperation> = operationsByName.toMap()

    fun operationNamed(operationName: String): SourceOperation? {
        return operationsByName[operationName]
    }

    fun availableOperationNames(): List<String> {
        return operationsByName.keys.sorted()
    }
}
