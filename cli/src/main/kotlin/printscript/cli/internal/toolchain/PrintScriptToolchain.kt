package printscript.cli.internal.toolchain

import printscript.formatter.Formatter
import printscript.interpreter.Interpreter
import printscript.linter.Linter
import printscript.runtime.ProgramOutput
import printscript.source.SourceReader
import printscript.statement.StatementSource
import printscript.token.TokenSource

internal class PrintScriptToolchain(
    val statementsFrom: (SourceReader) -> StatementSource,
    val formattingTokensFrom: (SourceReader) -> TokenSource,
    val interpreterWriting: (ProgramOutput) -> Interpreter,
    val formatter: () -> Formatter,
    val linter: () -> Linter,
)
