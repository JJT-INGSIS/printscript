package printscript.cli.internal.progress

import printscript.statement.StatementReadResult
import printscript.statement.StatementSource

private const val COMPLETE_PERCENTAGE = 100
private const val REPORTING_STEP = 10

/**
 * Decorator que informa el avance del parseo sin que ningún consumidor
 * se entere.
 *
 * El intérprete, el formatter y el analizador reciben un
 * [StatementSource] común y corriente: no hay ni un callback en sus
 * interfaces. Toda la responsabilidad del progreso queda del lado del
 * CLI, que es a quien le corresponde.
 *
 * El porcentaje sale del offset donde termina cada sentencia sobre el
 * largo total del archivo, no de contar sentencias — no se sabe cuántas
 * hay hasta terminar, pero sí cuántos caracteres tiene el archivo.
 *
 * Es inmutable: el último porcentaje informado viaja en la instancia
 * que se devuelve, igual que la fuente restante. Si no se re-envolviera
 * la fuente en cada lectura, el progreso se informaría una sola vez.
 */
internal class ProgressReportingStatementSource(
    private val delegate: StatementSource,
    private val totalCharacters: Long,
    private val lastReportedPercentage: Int = 0,
    private val onProgress: (Int) -> Unit,
) : StatementSource {

    override fun nextStatement(): StatementReadResult {
        val readResult = delegate.nextStatement()

        if (readResult !is StatementReadResult.Success) {
            return readResult
        }

        val percentage = percentageAt(readResult.statement.span.end.offset)
        val shouldReport = crossedReportingStep(percentage)

        if (shouldReport) {
            onProgress(percentage)
        }

        return readResult.copy(
            remainingSource = ProgressReportingStatementSource(
                delegate = readResult.remainingSource,
                totalCharacters = totalCharacters,
                lastReportedPercentage = if (shouldReport) percentage else lastReportedPercentage,
                onProgress = onProgress,
            ),
        )
    }

    /**
     * Solo se informa al cruzar cada decena, para que un archivo grande
     * no llene la pantalla con una línea por sentencia.
     */
    private fun crossedReportingStep(percentage: Int): Boolean {
        return percentage / REPORTING_STEP > lastReportedPercentage / REPORTING_STEP
    }

    private fun percentageAt(offset: Long): Int {
        if (totalCharacters <= 0) {
            return COMPLETE_PERCENTAGE
        }

        return ((offset * COMPLETE_PERCENTAGE) / totalCharacters)
            .toInt()
            .coerceIn(0, COMPLETE_PERCENTAGE)
    }
}
