package printscript.cli.internal.progress

import printscript.statement.StatementReadResult
import printscript.statement.StatementSource

private const val COMPLETE_PERCENTAGE = 100
private const val REPORTING_STEP = 10

internal class ProgressReportingStatementSource(
    private val delegate: StatementSource,
    private val totalBytes: Long,
    private val lastReportedPercentage: Int = 0,
    private val onProgress: (Int) -> Unit,
) : StatementSource {

    override fun nextStatement(): StatementReadResult {
        return when (val readResult = delegate.nextStatement()) {
            StatementReadResult.EndOfInput -> {
                reportCompletion()

                readResult
            }

            is StatementReadResult.Failure -> readResult

            is StatementReadResult.Success -> trackProgressOf(readResult)
        }
    }

    private fun trackProgressOf(readResult: StatementReadResult.Success): StatementReadResult {
        val percentage = percentageAt(readResult.statement.span.end.offset)
        val shouldReport = crossedReportingStep(percentage)

        if (shouldReport) {
            onProgress(percentage)
        }

        return readResult.copy(
            remainingSource = ProgressReportingStatementSource(
                delegate = readResult.remainingSource,
                totalBytes = totalBytes,
                lastReportedPercentage = if (shouldReport) percentage else lastReportedPercentage,
                onProgress = onProgress,
            ),
        )
    }

    private fun reportCompletion() {
        if (lastReportedPercentage < COMPLETE_PERCENTAGE) {
            onProgress(COMPLETE_PERCENTAGE)
        }
    }

    private fun crossedReportingStep(percentage: Int): Boolean {
        return percentage / REPORTING_STEP > lastReportedPercentage / REPORTING_STEP
    }

    private fun percentageAt(offset: Long): Int {
        if (totalBytes <= 0) {
            return COMPLETE_PERCENTAGE
        }

        return ((offset * COMPLETE_PERCENTAGE) / totalBytes)
            .toInt()
            .coerceIn(0, COMPLETE_PERCENTAGE)
    }
}
