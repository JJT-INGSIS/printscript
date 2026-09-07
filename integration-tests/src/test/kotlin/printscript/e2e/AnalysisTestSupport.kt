package printscript.e2e

import printscript.linter.Diagnostic
import printscript.linter.DiagnosticReadResult
import printscript.linter.DiagnosticSource
import printscript.v1.linter.PrintScriptV11LinterConfiguration
import printscript.v1.linter.PrintScriptV11LinterFactory
import printscript.v1.linter.PrintScriptV1LinterConfiguration
import printscript.v1.linter.PrintScriptV1LinterFactory

internal fun lintV1ScriptFromStream(
    sourceCode: String,
    bufferSizeInCharacters: Int,
    configuration: PrintScriptV1LinterConfiguration,
): ProgramAnalysis {
    val statements = v1StatementsFrom(streamReaderFor(sourceCode, bufferSizeInCharacters))

    return collectDiagnostics(
        source = PrintScriptV1LinterFactory.create(configuration).lint(statements),
    )
}

internal fun lintV11ScriptFromStream(
    sourceCode: String,
    bufferSizeInCharacters: Int,
    configuration: PrintScriptV11LinterConfiguration,
): ProgramAnalysis {
    val statements = v11StatementsFrom(streamReaderFor(sourceCode, bufferSizeInCharacters))

    return collectDiagnostics(
        source = PrintScriptV11LinterFactory.create(configuration).lint(statements),
    )
}

private tailrec fun collectDiagnostics(
    source: DiagnosticSource,
    diagnostics: List<Diagnostic> = emptyList(),
): ProgramAnalysis {
    return when (val read = source.nextDiagnostic()) {
        DiagnosticReadResult.EndOfInput ->
            ProgramAnalysis.Success(diagnostics)

        is DiagnosticReadResult.Failure ->
            ProgramAnalysis.Failure(read.error)

        is DiagnosticReadResult.Success ->
            collectDiagnostics(
                source = read.remainingSource,
                diagnostics = diagnostics + read.diagnostic,
            )
    }
}
