package printscript.cli.internal.toolchain

import printscript.formatter.Formatter
import printscript.interpreter.Interpreter
import printscript.linter.Linter
import printscript.source.SourceReader
import printscript.statement.StatementSource
import printscript.token.TokenSource
import printscript.v1.validation.Validator

internal class PrintScriptToolchain(
    val statementsFrom: (SourceReader) -> StatementSource,
    val formattingTokensFrom: (SourceReader) -> TokenSource,
    val interpreterUsing: (ProgramEnvironment) -> Interpreter,
    val validator: Validator,
    val formatterConfiguredBy: (String?) -> ConfiguredToolResult<Formatter>,
    val linterConfiguredBy: (String?) -> ConfiguredToolResult<Linter>,
)
