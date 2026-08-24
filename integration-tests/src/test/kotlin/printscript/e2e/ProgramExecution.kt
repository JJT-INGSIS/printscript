package printscript.e2e

import printscript.interpreter.InterpretationResult

internal data class ProgramExecution(
    val result: InterpretationResult,
    val outputLines: List<String>,
)
