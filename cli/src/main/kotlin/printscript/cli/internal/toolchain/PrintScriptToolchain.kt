package printscript.cli.internal.toolchain

import printscript.formatter.Formatter
import printscript.interpreter.Interpreter
import printscript.linter.Linter
import printscript.runtime.EnvironmentVariableProvider
import printscript.runtime.ProgramInput
import printscript.runtime.ProgramOutput
import printscript.source.SourceReader
import printscript.statement.StatementSource
import printscript.token.TokenSource

internal class PrintScriptToolchain(
    val statementsFrom: (SourceReader) -> StatementSource,
    val formattingTokensFrom: (SourceReader) -> TokenSource,
    val interpreterUsing: (ProgramOutput, ProgramInput, EnvironmentVariableProvider) -> Interpreter,
    val formatterConfiguredBy: (String?) -> ConfiguredToolResult<Formatter>,
    val linterConfiguredBy: (String?) -> ConfiguredToolResult<Linter>,
)

internal sealed interface ConfiguredToolResult<out T> {

    data class Success<T>(
        val tool: T,
    ) : ConfiguredToolResult<T>

    data class Failure(
        val reason: String,
    ) : ConfiguredToolResult<Nothing>
}
