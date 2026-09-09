package printscript.cli.internal.toolchain

import printscript.formatter.Formatter
import printscript.interpreter.Interpreter
import printscript.lexer.Lexer
import printscript.linter.Linter
import printscript.parser.Parser
import printscript.source.SourceReader
import printscript.statement.StatementSource
import printscript.token.TokenSource
import printscript.v1.validation.Validator

internal class PrintScriptToolchain(
    private val lexer: Lexer,
    private val parser: Parser,
    val interpreterUsing: (ProgramEnvironment) -> Interpreter,
    val validator: Validator,
    val formatterConfiguredBy: (String?) -> ConfiguredToolResult<Formatter>,
    val linterConfiguredBy: (String?) -> ConfiguredToolResult<Linter>,
) {

    fun tokensFrom(sourceReader: SourceReader): TokenSource {
        return lexer.tokenize(sourceReader)
    }

    fun statementsFrom(sourceReader: SourceReader): StatementSource {
        return parser.parse(tokensFrom(sourceReader))
    }
}
